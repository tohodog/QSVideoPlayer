package org.song.videoplayer;

import java.util.Map;


public class QSVideo {


    private String url;
    private String title;

    private String definition;
    private String resolution;
    private Map<String, String> headers;
    private Object option;


    public String url() {
        return url;
    }

    public String title() {
        return title;
    }

    public String definition() {
        return definition;
    }

    public String resolution() {
        return resolution;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Object option() {
        return option;
    }

    public static Builder Build(String url) {
        return new Builder(url);
    }

    public static final class Builder {

        private String url;
        private String title;

        private String definition;
        private String resolution;
        private Map<String, String> headers;
        private Object option;

        private Builder(String url) {
            this.url = url;
        }

        public QSVideo build() {
            QSVideo qsVideo = new QSVideo();
            qsVideo.url = url;
            qsVideo.title = title;
            qsVideo.definition = definition;
            qsVideo.resolution = resolution;
            qsVideo.headers = headers;
            qsVideo.option = option;
            return qsVideo;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;

        }

        public Builder definition(String definition) {
            this.definition = definition;
            return this;

        }

        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;

        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;

        }

        public Builder option(Object option) {
            this.option = option;
            return this;

        }
    }
}
