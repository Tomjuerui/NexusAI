package com.moyz.nexus.common.file;

import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.entity.NexusFile;
import com.moyz.nexus.common.service.SysConfigService;
import com.moyz.nexus.common.vo.SaveRemoteImageResult;
import dev.langchain4j.data.document.Document;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static com.moyz.nexus.common.cosntant.NexusConstant.STORAGE_LOCATION_VALUE_ALI_OSS;
import static com.moyz.nexus.common.cosntant.NexusConstant.STORAGE_LOCATION_VALUE_LOCAL;

public class FileOperatorContext {

    private static final Map<Integer, IFileOperator> CONCRETE_OPT = new HashMap<>();

    static {
        CONCRETE_OPT.put(STORAGE_LOCATION_VALUE_LOCAL, new LocalFileOperator());
        CONCRETE_OPT.put(STORAGE_LOCATION_VALUE_ALI_OSS, new AliyunOssFileOperator());
    }

    private final IFileOperator currentOpt;

    public FileOperatorContext(Integer storageLocation) {
        this.currentOpt = CONCRETE_OPT.get(storageLocation);
    }

    public FileOperatorContext() {
        Integer storageLocation = SysConfigService.getIntByKey(NexusConstant.SysConfigKey.STORAGE_LOCATION, -1);
        this.currentOpt = CONCRETE_OPT.get(storageLocation);
    }

    public static boolean checkIfExist(NexusFile NexusFile) {
        return CONCRETE_OPT.get(NexusFile.getStorageLocation()).checkIfExist(NexusFile);
    }

    public Pair<String, String> save(MultipartFile file, boolean image, String fileName) {
        return currentOpt.save(file, image, fileName);
    }

    public Pair<String, String> save(byte[] file, boolean image, String fileName) {
        return currentOpt.save(file, image, fileName);
    }

    public SaveRemoteImageResult saveImageFromUrl(String imageUrl, String uuid) {
        return currentOpt.saveImageFromUrl(imageUrl, uuid);
    }

    public static void delete(NexusFile NexusFile) {
        CONCRETE_OPT.get(NexusFile.getStorageLocation()).delete(NexusFile);
    }

    public static String getFileUrl(NexusFile NexusFile) {
        return CONCRETE_OPT.get(NexusFile.getStorageLocation()).getFileUrl(NexusFile);
    }

    public static Document loadDocument(NexusFile NexusFile) {
        return CONCRETE_OPT.get(NexusFile.getStorageLocation()).loadDocument(NexusFile);
    }

    public static int getStorageLocation() {
        return SysConfigService.getIntByKey(NexusConstant.SysConfigKey.STORAGE_LOCATION, -1);
    }
}
