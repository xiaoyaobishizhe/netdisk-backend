package com.xiaoyao.netdisk.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyao.netdisk.common.exception.E;
import com.xiaoyao.netdisk.common.exception.NetdiskException;
import com.xiaoyao.netdisk.common.web.interceptor.TokenInterceptor;
import com.xiaoyao.netdisk.file.dto.FileListDTO;
import com.xiaoyao.netdisk.file.dto.ListSharesDTO;
import com.xiaoyao.netdisk.file.properties.ShareProperties;
import com.xiaoyao.netdisk.file.repository.ShareRepository;
import com.xiaoyao.netdisk.file.repository.UserFileRepository;
import com.xiaoyao.netdisk.file.repository.entity.Share;
import com.xiaoyao.netdisk.file.service.ShareService;
import com.xiaoyao.netdisk.file.service.UserFileService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ShareServiceImpl implements ShareService {
    private final ShareRepository shareRepository;
    private final UserFileRepository userFileRepository;
    private final ShareProperties shareProperties;
    private final UserFileService userFileService;

    public ShareServiceImpl(ShareRepository shareRepository, UserFileRepository userFileRepository,
                            ShareProperties shareProperties, UserFileService userFileService) {
        this.shareRepository = shareRepository;
        this.userFileRepository = userFileRepository;
        this.shareProperties = shareProperties;
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
            item.setTimeout(share.getTimeout());
            items.add(item);
        });
        return dto;
    }

    @Override
    public String createShare(String name, String password, int timeout, List<Long> fileList) {
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
        return StrUtil.format(shareProperties.getUrlMode(), Map.of(
                "code", share.getCode(),
                "pwd", share.getPassword()
        ));
    }

    @Override
    public String getShareLink(String id) {
        long userId = TokenInterceptor.USER_ID.get();
        Share share = shareRepository.findShareLink(Long.parseLong(id), userId);
        if (share == null) {
            throw new NetdiskException(E.SHARE_NOT_EXIST);
        } else if (share.getTimeout() != 0 && share.getCreateTime().plusDays(share.getTimeout()).isBefore(LocalDateTime.now())) {
            throw new NetdiskException(E.SHARE_TIMEOUT);
        }
        return StrUtil.format(shareProperties.getUrlMode(), Map.of(
                "code", share.getCode(),
                "pwd", share.getPassword()
        ));
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
}
