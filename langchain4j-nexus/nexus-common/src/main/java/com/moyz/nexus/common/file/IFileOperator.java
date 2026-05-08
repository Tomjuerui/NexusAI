package com.moyz.nexus.common.file;

import com.moyz.nexus.common.entity.NexusFile;
import com.moyz.nexus.common.vo.SaveRemoteImageResult;
import dev.langchain4j.data.document.Document;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

public interface IFileOperator {
    boolean checkIfExist(NexusFile NexusFile);

    /**
     * @param file     文件
     * @param image    是否图片
     * @param fileName 文件名（不带后缀时根据file获取后缀并追加到fileName�?
     * @return 文件路径及后缀
     */
    Pair<String, String> save(MultipartFile file, boolean image, String fileName);

    /**
     * @param file     文件
     * @param image    是否图片
     * @param fileName 文件�?
     * @return 文件路径及后缀
     */
    Pair<String, String> save(byte[] file, boolean image, String fileName);

    SaveRemoteImageResult saveImageFromUrl(String imageUrl, String uuid);

    void delete(NexusFile NexusFile);

    String getFileUrl(NexusFile NexusFile);

    Document loadDocument(NexusFile NexusFile);
}
