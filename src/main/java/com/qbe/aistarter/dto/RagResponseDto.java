package com.qbe.aistarter.dto;

import java.util.List;

public record RagResponseDto(String answer, List<SourceInfoDto> sources) {}
