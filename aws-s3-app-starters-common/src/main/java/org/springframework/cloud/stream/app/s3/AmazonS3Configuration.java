/*
 * Copyright 2016-2017 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.aws.context.annotation.ConditionalOnMissingAmazonClient;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * @author Artem Bilan
 */
@Configuration
@ConditionalOnMissingAmazonClient(AmazonS3.class)
public class AmazonS3Configuration {
	@Value("${cloud.aws.credentials.endpointUrl}")
	private String endpointUrl;
	@Bean
	@ConditionalOnMissingBean
	public AmazonS3 amazonS3(AWSCredentialsProvider awsCredentialsProvider, RegionProvider regionProvider) {
		
		System.out.println(">>>>endpointUrl property>>"+endpointUrl);
		return AmazonS3ClientBuilder.standard()
				.withCredentials(awsCredentialsProvider)
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, Regions.US_EAST_1.name()))
//				.withRegion(regionProvider.getRegion().getName())
				.build();
	}

}
