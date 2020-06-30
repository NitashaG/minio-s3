package org.vmware.s3.pdf.custom;

import java.util.Collections;

import org.springframework.cloud.stream.app.file.FileConsumerProperties;
import org.springframework.cloud.stream.app.file.FileReadingMode;
import org.springframework.integration.dsl.IntegrationFlowBuilder;

public class FileUtils {

	public static IntegrationFlowBuilder enhanceStreamFlowForReadingMode(IntegrationFlowBuilder flowBuilder,
			FileConsumerProperties fileConsumerProperties) {
		return org.springframework.cloud.stream.app.file.FileUtils.enhanceStreamFlowForReadingMode( flowBuilder,
				 fileConsumerProperties);
	}
	public static  IntegrationFlowBuilder enhanceFlowForReadingMode(IntegrationFlowBuilder flowBuilder,
			FileConsumerProperties fileConsumerProperties) {
		System.out.println("**** Entering Custom FileUtils");
		if(fileConsumerProperties.getMode()==FileReadingMode.contents) {
			return ((IntegrationFlowBuilder)flowBuilder.enrichHeaders(Collections.singletonMap("contentType", "application/octet-stream"))).split(new PDFSplitter());
		}
		return org.springframework.cloud.stream.app.file.FileUtils.enhanceFlowForReadingMode( flowBuilder,
				 fileConsumerProperties);
	}
}
