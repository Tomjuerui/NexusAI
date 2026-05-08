package com.moyz.nexus.common.languagemodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.entity.ModelPlatform;
import com.moyz.nexus.common.file.FileOperatorContext;
import com.moyz.nexus.common.languagemodel.data.SiliconflowTtsJobData;
import com.moyz.nexus.common.languagemodel.data.SiliconflowTtsReq;
import com.moyz.nexus.common.util.JsonUtil;
import com.moyz.nexus.common.util.LocalDateTimeUtil;
import com.moyz.nexus.common.util.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.moyz.nexus.common.cosntant.NexusConstant.TtsConstant.SILICONFLOW_DEFAULT_VOICE;
import static com.moyz.nexus.common.util.LocalDateTimeUtil.PATTERN_YYYYMMDDMMHHSS;

/**
 * 硅基流动�?TTS 服务，对应的接口不能流式输入，只能流式输�?
 */
@Slf4j
public class SiliconflowTtsService extends AbstractTtsModelService {
    private final Map<String, SiliconflowTtsJobData> jobToData = new HashMap<>();

    public SiliconflowTtsService(AiModel model, ModelPlatform modelPlatform) {
        super(model, modelPlatform);
    }

    @Override
    public void start(String jobId, String voice, Consumer<ByteBuffer> onProcess, Consumer<String> onComplete, Consumer<String> onError) {
        if (StringUtils.isBlank(voice)) {
            voice = SILICONFLOW_DEFAULT_VOICE;
        } else {
            // 相关参数：https://docs.siliconflow.cn/cn/api-reference/audio/create-speech#cosyvoice2-0-5b
            boolean match = false;
            ObjectNode node = aiModel.getProperties();
            if (node.has("voices") && node.get("voices").isArray()) {
                ArrayNode voices = node.withArray("voices");
                for (JsonNode voiceNode : voices) {
                    if (voice.equals(voiceNode.get("name").asText())) {
                        match = true;
                        break;
                    }
                }
            }
            if (!match) {
                voice = SILICONFLOW_DEFAULT_VOICE;
            }
        }
        SiliconflowTtsJobData jobData = new SiliconflowTtsJobData();
        jobData.setVoice(voice);
        jobData.setText(new StringBuilder());
        jobData.setProcessCallback(onProcess);
        jobData.setCompleteCallback(onComplete);
        jobData.setErrorCallback(onError);
        jobToData.put(jobId, jobData);
    }

    @Override
    public void processByStream(String jobId, String partText) {
        if (null == jobToData.get(jobId)) {
            return;
        }
        jobToData.get(jobId).getText().append(partText);
    }

    @Override
    public void complete(String jobId) {
        if (null == jobToData.get(jobId)) {
            return;
        }
        SiliconflowTtsJobData jobData = jobToData.get(jobId);
        jobToData.remove(jobId);
        SiliconflowTtsReq.SiliconflowTtsReqBuilder requestBuilder = SiliconflowTtsReq.builder()
                .input(jobData.getText().toString())
                .model(aiModel.getName())
                .voice(jobData.getVoice())
                .stream(true);
        if (StringUtils.isNotBlank(jobData.getVoice())) {
            requestBuilder.voice(jobData.getVoice());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + platform.getApiKey());
        RestTemplate restTemplate = new RestTemplate();
        String url = platform.getBaseUrl() + "/audio/speech";
        // 使用流式处理
        ResponseExtractor<Void> responseExtractor = response -> {
            try (InputStream inputStream = response.getBody()) {
                String datetime = LocalDateTimeUtil.format(LocalDateTime.now(), PATTERN_YYYYMMDDMMHHSS);
                String fileName = aiModel.getName().toLowerCase().replace("/", "-") + "-" + datetime + "-" + UuidUtil.createShort().substring(0, 6) + ".mp3";
                // 创建临时文件
                Path tempFile = Files.createTempFile("tmp-tts-" + datetime, ".mp3");
                ByteArrayOutputStream sendBuffer = new ByteArrayOutputStream();
                final int MIN_BUFFER_SIZE = 16 * 1024; // 最小缓冲大�?
                // 使用缓冲流写入文�?
                try (OutputStream outputStream = Files.newOutputStream(tempFile);
                     BufferedOutputStream bufferedOutput = new BufferedOutputStream(outputStream)) {
                    byte[] buffer = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bufferedOutput.write(buffer, 0, bytesRead);
                        sendBuffer.write(buffer, 0, bytesRead);
                        if (sendBuffer.size() >= MIN_BUFFER_SIZE) {
                            // 发送缓冲区内容
                            jobData.getProcessCallback().accept(ByteBuffer.wrap(sendBuffer.toByteArray()));
                            // 重置缓冲�?
                            sendBuffer.reset();
                        }
                    }
                    if (sendBuffer.size() > 0) {
                        jobData.getProcessCallback().accept(ByteBuffer.wrap(sendBuffer.toByteArray()));
                    }
                }
                Pair<String, String> pair = new FileOperatorContext().save(Files.readAllBytes(tempFile), false, fileName);
                log.info("保存文件成功，路径为�? + pair.getLeft());
                jobData.getCompleteCallback().accept(pair.getLeft());
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }
            return null;
        };
        restTemplate.execute(url, HttpMethod.POST, requestCallback -> {
            requestCallback.getHeaders().putAll(headers);
            requestCallback.getBody().write(JsonUtil.toJson(requestBuilder.build()).getBytes());
        }, responseExtractor);

    }
}
