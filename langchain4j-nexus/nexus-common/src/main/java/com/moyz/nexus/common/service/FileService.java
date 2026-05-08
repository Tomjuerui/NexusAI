package com.moyz.nexus.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.nexus.common.base.ThreadContext;
import com.moyz.nexus.common.entity.NexusFile;
import com.moyz.nexus.common.entity.User;
import com.moyz.nexus.common.enums.ErrorEnum;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.file.FileOperatorContext;
import com.moyz.nexus.common.file.LocalFileUtil;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.mapper.FileMapper;
import com.moyz.nexus.common.util.HashUtil;
import com.moyz.nexus.common.util.UuidUtil;
import com.moyz.nexus.common.vo.SaveRemoteImageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.moyz.nexus.common.enums.ErrorEnum.A_AI_IMAGE_NO_AUTH;
import static com.moyz.nexus.common.enums.ErrorEnum.A_FILE_NOT_EXIST;
import static com.moyz.nexus.common.enums.ErrorEnum.A_UPLOAD_FILE_TYPE_NOT_ALLOWED;
import static com.moyz.nexus.common.enums.ErrorEnum.A_UPLOAD_IMAGE_TYPE_NOT_ALLOWED;

@Slf4j
@Service
public class FileService extends ServiceImpl<FileMapper, NexusFile> {

    @Value("${local.images}")
    private String imagePath;

    @Value("${local.watermark-images}")
    private String watermarkImagesPath;

    @Value("${local.thumbnails}")
    private String thumbnailsPath;

    @Value("${local.watermark-thumbnails}")
    private String watermarkThumbnailsPath;

    @Value("${local.files}")
    private String filePath;

    @Value("${local.tmp-images}")
    private String tmpImagesPath;

    private static final List<String> BINARY_EXTENSIONS = List.of(
            // 可执�?二进�?
            "exe", "dll", "so", "dylib", "com", "msi",
            "jar", "war", "ear", "class",
            "iso", "dmg", "bin", "apk", "ipa", "deb", "rpm",
            // 压缩�?
            "zip", "rar", "7z", "tar", "gz", "bz2",
            // 视频
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "m4v", "mpeg", "3gp"
    );

    public NexusFile saveFile(MultipartFile file, boolean image) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename) || !originalFilename.contains(".")) {
            throw new BaseException(A_UPLOAD_FILE_TYPE_NOT_ALLOWED);
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (BINARY_EXTENSIONS.contains(ext)) {
            throw new BaseException(A_UPLOAD_FILE_TYPE_NOT_ALLOWED);
        }
        if (image && !NexusConstant.IMAGE_EXTENSIONS.contains(ext)) {
            throw new BaseException(A_UPLOAD_IMAGE_TYPE_NOT_ALLOWED);
        }
        String sha256 = HashUtil.sha256(file);
        Optional<NexusFile> existFile = this.lambdaQuery()
                .eq(NexusFile::getSha256, sha256)
                .eq(NexusFile::getIsDeleted, false)
                .last("limit 1")
                .oneOpt();
        if (existFile.isPresent()) {
            NexusFile NexusFile = existFile.get();
            boolean exist = FileOperatorContext.checkIfExist(NexusFile);
            if (exist) {
                return NexusFile;
            } else {
                log.warn("文件不存�?删除记录以便后续重新生成,fileId:{},uuid:{},sha256:{}", NexusFile.getId(), NexusFile.getUuid(), NexusFile.getSha256());
                this.lambdaUpdate().eq(NexusFile::getId, NexusFile.getId()).set(NexusFile::getIsDeleted, true).update();
            }
        }
        String uuid = UuidUtil.createShort();
        Pair<String, String> originalFile = new FileOperatorContext().save(file, image, uuid);
        NexusFile NexusFile = new NexusFile();
        NexusFile.setName(file.getOriginalFilename());
        NexusFile.setUuid(uuid);
        NexusFile.setSha256(sha256);
        NexusFile.setPath(originalFile.getLeft());
        NexusFile.setExt(originalFile.getRight());
        NexusFile.setUserId(ThreadContext.getCurrentUserId());
        NexusFile.setStorageLocation(FileOperatorContext.getStorageLocation());
        this.getBaseMapper().insert(NexusFile);
        return NexusFile;
    }

    public NexusFile saveImageFromUrl(User user, String sourceImageUrl) {
        log.info("saveImageFromUrl,sourceImageUrl:{}", sourceImageUrl);
        String uuid = UuidUtil.createShort();
        SaveRemoteImageResult saveResult = new FileOperatorContext().saveImageFromUrl(sourceImageUrl, uuid);
        NexusFile NexusFile = new NexusFile();
        NexusFile.setName(saveResult.getOriginalName());
        NexusFile.setUuid(uuid);
        NexusFile.setSha256(HashUtil.sha256(saveResult.getPathOrUrl()));
        NexusFile.setPath(saveResult.getPathOrUrl());
        NexusFile.setUserId(user.getId());
        NexusFile.setExt(saveResult.getExt());
        NexusFile.setStorageLocation(FileOperatorContext.getStorageLocation());
        this.getBaseMapper().insert(NexusFile);
        return NexusFile;
    }

    public NexusFile saveFromPath(User user, String pathOrUrl) {
        log.info("saveImageFromPath,path:{}", pathOrUrl);
        Pair<String, String> nameAndExt = LocalFileUtil.getNameAndExt(pathOrUrl);
        String uuid = UuidUtil.createShort();
        NexusFile NexusFile = new NexusFile();
        NexusFile.setName(nameAndExt.getLeft());
        NexusFile.setUuid(uuid);
        NexusFile.setSha256(HashUtil.sha256(pathOrUrl));
        NexusFile.setPath(pathOrUrl);
        NexusFile.setUserId(user.getId());
        NexusFile.setExt(nameAndExt.getRight());
        NexusFile.setStorageLocation(FileOperatorContext.getStorageLocation());
        this.getBaseMapper().insert(NexusFile);
        return NexusFile;
    }

    public boolean softDel(String uuid) {
        return this.lambdaUpdate()
                .eq(NexusFile::getUserId, ThreadContext.getCurrentUserId())
                .eq(NexusFile::getUuid, uuid)
                .set(NexusFile::getIsDeleted, true)
                .update();
    }

    public boolean removeFileAndSoftDel(String uuid) {
        NexusFile NexusFile = this.lambdaQuery()
                .eq(NexusFile::getUserId, ThreadContext.getCurrentUserId())
                .eq(NexusFile::getUuid, uuid)
                .oneOpt()
                .orElse(null);
        if (null == NexusFile) {
            return false;
        }
        FileOperatorContext.delete(NexusFile);
        return this.softDel(uuid);
    }

    public NexusFile getByUuid(String uuid) {
        return this.lambdaQuery()
                .eq(NexusFile::getUuid, uuid)
                .eq(NexusFile::getUserId, ThreadContext.getCurrentUserId())
                .oneOpt().orElse(null);
    }

    /**
     * 读取图片到BufferedImage，管理员或图片拥有者才有权限查�?
     *
     * @param uuid      图片uuid
     * @param thumbnail 读取的是缩略�?
     * @return 图片内容
     */
    public BufferedImage readMyImage(String uuid, boolean thumbnail) {
        if (StringUtils.isBlank(ThreadContext.getToken())) {
            throw new BaseException(A_AI_IMAGE_NO_AUTH);
        }
        NexusFile NexusFile = this.lambdaQuery()
                .eq(!ThreadContext.getCurrentUser().getIsAdmin(), NexusFile::getUserId, ThreadContext.getCurrentUserId())
                .eq(NexusFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == NexusFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        return LocalFileUtil.readLocalImage(NexusFile, thumbnail, thumbnailsPath);
    }

    public BufferedImage readImage(String uuid, boolean thumbnail) {
        NexusFile NexusFile = this.lambdaQuery()
                .eq(NexusFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == NexusFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        return LocalFileUtil.readLocalImage(NexusFile, thumbnail, thumbnailsPath);
    }

    public String getImagePath(String uuid) {
        NexusFile NexusFile = this.lambdaQuery()
                .eq(NexusFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == NexusFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return NexusFile.getPath();
    }


    public NexusFile getFile(String uuid) {
        NexusFile NexusFile = this.lambdaQuery()
                .eq(NexusFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == NexusFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return NexusFile;
    }

    public String getTmpImagesPath(String uuid) {
        NexusFile NexusFile = this.lambdaQuery()
                .eq(NexusFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == NexusFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return tmpImagesPath + uuid + "." + NexusFile.getExt();
    }

    public String getWatermarkImagesPath(String uuid) {
        NexusFile NexusFile = this.lambdaQuery()
                .eq(NexusFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == NexusFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return watermarkImagesPath + uuid + "." + NexusFile.getExt();
    }

    public String getWatermarkImagesPath(NexusFile NexusFile) {
        return watermarkImagesPath + NexusFile.getUuid() + "." + NexusFile.getExt();
    }

    public String getUrl(String fileUuid) {
        if (StringUtils.isBlank(fileUuid)) {
            return null;
        }
        List<String> list = getUrls(List.of(fileUuid));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取文件url
     *
     * @param fileUuids 文件uuid
     * @return 文件url
     */
    public List<String> getUrls(List<String> fileUuids) {
        if (CollectionUtils.isEmpty(fileUuids)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        this.lambdaQuery()
                .in(NexusFile::getUuid, fileUuids)
                .eq(NexusFile::getIsDeleted, false)
                .list()
                .forEach(NexusFile -> {
                    result.add(FileOperatorContext.getFileUrl(NexusFile));
                });
        return result;
    }
}
