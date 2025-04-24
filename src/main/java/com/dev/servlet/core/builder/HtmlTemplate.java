package com.dev.servlet.core.builder;

import lombok.Builder;

import java.util.Optional;
@Builder(builderClassName = "HtmlTemplateBuilder", builderMethodName = "newBuilder")
public class HtmlTemplate {
    private String pageTitle;
    private String title;
    private String subTitle;
    private String message;
    private String link;
    private String linkName;
    private String image;
    private String info;
    private String styleCss;
    public static class HtmlTemplateBuilder {

        public HtmlTemplateBuilder error(int status) {
            this.pageTitle = "Error " + status;
            this.title = "Error";
            this.info = "To help us improve, please report this error to the administrator.";
            return this;
        }

        public HtmlTemplateBuilder pageInfo() {
            this.pageTitle = "Info";
            this.title = "Info";
            this.info = "If you need help, please contact the administrator.";
            return this;
        }

        public String build() {
            String templateCSS = getTemplateStyle();
            if (styleCss != null) {
                templateCSS = templateCSS.concat(styleCss);
            }
            String templateHTML = getTemplateHTML();
            templateHTML = templateHTML
                    .replace("{pageTitle}", getReplacement(pageTitle))
                    .replace("{title}", title)
                    .replace("{subTitle}", getReplacement(subTitle))
                    .replace("{message}", message)
                    .replace("{info}", getReplacement(info))
                    .replace("{image}", getReplacement(image))
                    .replace("{link}", getReplacement(link))
                    .replace("{linkName}", getReplacement(linkName))
                    .replace("{styleCss}", templateCSS);
            return templateHTML.replace("\n", "");
        }

        private String getTemplateHTML() {
            StringBuilder htmlTemplate = new StringBuilder();
            htmlTemplate.append("<html lang=\"en\">")
                    .append("<head><title>{pageTitle}</title></head>")
                    .append("<style>{styleCss}</style>")
                    .append("<body>")
                    .append("<div class=\"container\">")
                    .append("<h1>{title}</h1>")
                    .append("<h2>{subTitle}</h2>")
                    .append("<p>{message}</p>");
            if (link != null && !link.isEmpty()) {
                linkName = Optional.ofNullable(linkName).orElse("See more");
                htmlTemplate.append("<a class='link' href=\"{link}\">{linkName}</a>");
            }
            if (image != null && !image.isEmpty()) {
                htmlTemplate.append("<div class='image'>")
                        .append("<img src='{image}' alt='Error image' width='200' height='200' title='Error aaaah!'>")
                        .append("</div>");
            }
            htmlTemplate.append("<p class=\"user-message\">{info}</p>")
                    .append("<button onclick=\"history.back()\">")
                    .append("<span style='font-size:20px;'>&#8592;</span> ")
                    .append("Back")
                    .append("</button>")
                    .append("</div>")
                    .append("</body>")
                    .append("</html>");
            return htmlTemplate.toString().replace("\n", "");
        }


        private String getTemplateStyle() {
            String templateStyle = """
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f0f8ff;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                    }
                    h1 {
                        color: #e74c3c;
                    }
                    h2 {
                        color: #333;
                    }
                    p {
                        font-size: 16px;
                    }
                    button {
                        margin-top: 10px;
                        padding: 10px 20px;
                        font-size: 16px;
                        color: #fff;
                        background-color: #3498db;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                    }
                    button:hover {
                        background-color: #2980b9;
                    }
                    .container {
                        background-color: #fff;
                        padding: 75px;
                        min-width: 300px;
                        border-radius: 8px;
                        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                        text-align: center;
                    }
                    .user-message {
                        color: #555;
                        font-size: 12px;
                        margin-top: 40px;
                    }
                    .link {
                        color: #3498db;
                        font-size: 14px;
                        margin-top: 20px;
                    }
                    .image {
                        margin-top: 20px;
                        text-align: center !important;
                    }
                    """;
            return templateStyle.replace("\n", "");
        }


        private String getReplacement(String value) {
            return Optional.ofNullable(value).orElse("");
        }
    }
}
