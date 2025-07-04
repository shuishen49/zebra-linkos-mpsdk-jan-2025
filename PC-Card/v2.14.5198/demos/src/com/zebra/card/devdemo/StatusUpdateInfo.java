/*
 * CONFIDENTIAL AND PROPRIETARY
 *
 * The source code and other information contained herein is the confidential and exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 *
 * Copyright ZIH Corp. 2018
 *
 * ALL RIGHTS RESERVED
 */

package com.zebra.card.devdemo;

import com.zebra.sdk.common.card.containers.JobStatusInfo;

public class StatusUpdateInfo {

	private final JobStatusInfo jobStatusInfo;
	private final JobInfo jobInfo;
	private final String message;

	public StatusUpdateInfo(JobStatusInfo jobStatusInfo, JobInfo jobInfo, String message) {
		this.jobStatusInfo = jobStatusInfo;
		this.jobInfo = jobInfo;
		this.message = message;
	}

	public JobStatusInfo getJobStatusInfo() {
		return jobStatusInfo;
	}

	public JobInfo getJobInfo() {
		return jobInfo;
	}

	public String getMessage() {
		return message;
	}
}
