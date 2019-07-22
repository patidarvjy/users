package com.docutools.emails.tempaltes;

public class EmailTemplate {

    private String subjectLine;
    private String html;

    public EmailTemplate(String subjectLine, String html) {
        this.subjectLine = subjectLine;
        this.html = html;
    }

    public String getSubjectLine() {
        return subjectLine;
    }

    public String getHtml() {
        return this.html;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmailTemplate that = (EmailTemplate) o;

        if (subjectLine != null ? !subjectLine.equals(that.subjectLine) : that.subjectLine != null) return false;
        return html != null ? html.equals(that.html) : that.html == null;
    }

    @Override
    public int hashCode() {
        int result = subjectLine != null ? subjectLine.hashCode() : 0;
        result = 31 * result + (html != null ? html.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EmailTemplate{" +
                "subjectLine='" + subjectLine + '\'' +
                ", html='" + html + '\'' +
                '}';
    }
}
