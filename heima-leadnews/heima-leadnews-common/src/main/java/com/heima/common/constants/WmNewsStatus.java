package com.heima.common.constants;

import org.junit.runner.notification.Failure;

/**
 * Created on 2022/9/13.
 *
 * @author Chen Gao
 */
public interface WmNewsStatus {
    public static final Integer DRAFT = 0;
    public static final Integer SUBMIT = 1;
    public static final Integer AUDIT_FAILURE = 2;
    public static final Integer MANUAL_REVIEW = 3;
    public static final Integer MANUAL_REVIEW_PASS = 4;
    public static final Integer AUDIT_SUCCESS = 8;
    public static final Integer PUBLISHED = 9;
}
