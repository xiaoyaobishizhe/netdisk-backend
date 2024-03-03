package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.LinkInfoDTO;
import com.xiaoyao.netdisk.file.dto.ListSharesDTO;
import com.xiaoyao.netdisk.file.repository.ShareRepository;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.UserFileTreeNode;
import com.xiaoyao.netdisk.file.repository.entity.Share;
import com.xiaoyao.netdisk.file.service.ShareService;
import com.xiaoyao.netdisk.file.service.UserFileService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ShareServiceImpl implements ShareService {
    private final ShareRepository shareRepository;
    private final UserFileRepository userFileRepository;
    private final UserFileService userFileService;

    public ShareServiceImpl(ShareRepository shareRepository, UserFileRepository userFileRepository,
                            UserFileService userFileService) {
        this.shareRepository = shareRepository;
        this.userFileRepository = userFileRepository;
        this.userFileService = userFileService;
    }

    @Override
    public ListSharesDTO listShares() {
        long userId = TokenInterceptor.USER_ID.get();
        List<Share> shares = shareRepository.listShares(userId);

        ListSharesDTO dto = new ListSharesDTO();
        List<ListSharesDTO.Item> items = new ArrayList<>();
        dto.setItems(items);
        shares.forEach(share -> {
            ListSharesDTO.Item item = new ListSharesDTO.Item();
            item.setId(String.valueOf(share.getId()));
            item.setName(share.getName());
            item.setCreateTime(DateUtil.format(share.getCreateTime(), "yyyy-MM-dd HH:mm"));
            // 计算剩余过期天数，如果剩余天数不足一天则显示为1天。
            item.setStatus(share.getTimeout() == 0 ? "永久有效" : DateUtil.betweenDay(
                    Date.from(share.getCreateTime().plusDays(share.getTimeout()).atZone(ZoneId.systemDefault())
                            .toInstant()), new Date(), true) + "天后过期");
            items.add(item);
        });
        return dto;
    }

    @Override
    public LinkInfoDTO createShare(String name, String password, int timeout, List<Long> fileList) {
        long userId = TokenInterceptor.USER_ID.get();
        if (!userFileRepository.isAllExist(fileList, userId)) {
            throw new NetdiskException(E.FILE_NOT_EXIST);
        }
        Share share = new Share();
        share.setUserId(userId);
        share.setName(name);
        share.setCode(RandomUtil.randomString(30));
        share.setPassword(StrUtil.isEmpty(password) ? RandomUtil.randomString(RandomUtil.BASE_CHAR_NUMBER_LOWER, 4) : password);
        share.setToken(RandomUtil.randomString(100));
        share.setFileList(fileList);
        share.setTimeout(timeout);
        share.setCreateTime(LocalDateTime.now());
        shareRepository.save(share);
        return new LinkInfoDTO(share.getCode(), share.getPassword());
    }

    @Override
    public LinkInfoDTO getShareLink(String id) {
        long userId = TokenInterceptor.USER_ID.get();
        Share share = shareRepository.findShareLink(Long.parseLong(id), userId);
        if (share == null) {
            throw new NetdiskException(E.SHARE_NOT_EXIST);
        } else if (share.getTimeout() != 0 && share.getCreateTime().plusDays(share.getTimeout()).isBefore(LocalDateTime.now())) {
            throw new NetdiskException(E.SHARE_TIMEOUT);
        }
        return new LinkInfoDTO(share.getCode(), share.getPassword());
    }

    @Override
    public void deleteShare(List<String> ids) {
        shareRepository.deleteShare(ids.stream().map(Long::parseLong).toList(), TokenInterceptor.USER_ID.get());
    }

    @Override
    public String getAccessToken(String code, String password) {
        Share share = shareRepository.findAccessToken(code);
        if (share == null) {
            throw new NetdiskException(E.SHARE_NOT_EXIST);
        } else if (!share.getPassword().equals(password)) {
            throw new NetdiskException(E.SHARE_PASSWORD_ERROR);
        } else if (share.getTimeout() != 0 && share.getCreateTime().plusDays(share.getTimeout()).isBefore(LocalDateTime.now())) {
            throw new NetdiskException(E.SHARE_TIMEOUT);
        }
        return share.getToken();
    }

    @Override
    public FileListDTO list(String token, String parentId) {
        Long pid = parentId == null ? null : Long.parseLong(parentId);
        List<Long> ids = shareRepository.getFileList(token);

        if (pid == null) {
            return userFileService.list(ids);
        }

        // 如果目标文件夹路径在这些文件夹路径下则允许访问，否则拒绝访问。
        List<Long> t = new ArrayList<>(ids);
        t.add(pid);
        Map<Long, String> pathTable = userFileRepository.findPathByIds(t);
        String targetPath = pathTable.get(pid);
        List<String> paths = ids.stream().filter(pathTable::containsKey).map(pathTable::get).toList();
        if (paths.isEmpty()) {
            throw new NetdiskException(E.SHARE_FILE_DELETED);
        } else if (paths.stream().noneMatch(targetPath::startsWith)) {
            throw new NetdiskException(E.PERMISSION_DENIED);
        }

        return userFileService.list(parentId, false);
    }

    @Override
    public long getUserId(String code) {
        Long userId = shareRepository.getUserIdByCode(code);
        if (userId == null) {
            throw new NetdiskException(E.SHARE_NOT_EXIST);
        }
        return userId;
    }

    @Override
    public void save(String token, List<Long> ids, String parentId) {
        Long pid = parentId == null ? null : Long.parseLong(parentId);
        long userId = TokenInterceptor.USER_ID.get();
        List<Long> fileList = shareRepository.getFileList(token);

        List<Long> t = new ArrayList<>(fileList);
        t.addAll(ids);
        Map<Long, String> pathTable = userFileRepository.findPathByIds(t);
        List<String> paths = fileList.stream().filter(pathTable::containsKey).map(pathTable::get).toList();
        // 只要有一个不在允许的范围下就报错
        if (!ids.stream().allMatch(id -> paths.stream().anyMatch(pathTable.get(id)::startsWith))) {
            throw new NetdiskException(E.PERMISSION_DENIED);
        }

        List<UserFileTreeNode> trees = userFileRepository.findUserFileTrees(ids, false, null);

        // 确保文件都在同一路径下
        if (trees.stream()
                .map(node -> node.getValue().getParentId() == null ? 0 : node.getValue().getParentId())
                .distinct()
                .count() != 1) {
            throw new NetdiskException(E.FILE_NOT_SAME_PATH);
        }

        // 确保所有名称在目标文件夹下都不存在
        if (userFileRepository.isNameExistInParent(pid, trees.stream().map(node -> node.getValue().getName()).toList(), userId)) {
            throw new NetdiskException(E.FILE_NAME_ALREADY_EXIST);
        }

        // 重新组装文件从属关系
        String path = pid == null ? "/" : userFileRepository.getFolderFullPath(pid, userId);
        trees.forEach(tree -> {
            tree.getValue().setParentId(pid);
            tree.refreshIdAndPathDeeply(path, true);
            tree.refreshUserIdDeeply(userId);
        });

        userFileRepository.save(trees.stream().flatMap(tree -> tree.collectAll().stream()).toList());
    }
}
