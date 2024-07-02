package com.monthlyib.server.api.storage.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageSearchDto {

    private Long parentsFolderId;

    private String keyWord;

}
