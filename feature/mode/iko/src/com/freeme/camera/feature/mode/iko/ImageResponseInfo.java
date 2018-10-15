package com.freeme.camera.feature.mode.iko;

import java.io.Serializable;

/**
 * Created by azmohan on 18-3-13.
 */

public class ImageResponseInfo implements Serializable {
    public ResultInfo result;
    public String support;
    public String logid;

    public static class ResultInfo implements Serializable {
        public String url;
    }
}
