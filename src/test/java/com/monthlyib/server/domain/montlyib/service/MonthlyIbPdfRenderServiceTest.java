package com.monthlyib.server.domain.montlyib.service;

import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyIbPdfRenderServiceTest {

    @Test
    void render_generates_pdf_for_simple_html_content() {
        MonthlyIbPdfRenderService service = new MonthlyIbPdfRenderService();
        MonthlyIb monthlyIb = MonthlyIb.builder()
                .monthlyIbId(1L)
                .title("Monthly IB Demo")
                .content("<p>Hello Monthly IB</p>")
                .monthlyIbThumbnailFileId(0L)
                .monthlyIbThumbnailFileName("")
                .monthlyIbThumbnailFileUrl("")
                .build();

        byte[] pdf = service.render(monthlyIb);

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
