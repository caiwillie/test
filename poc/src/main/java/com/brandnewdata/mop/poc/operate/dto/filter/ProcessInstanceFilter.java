package com.brandnewdata.mop.poc.operate.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class ProcessInstanceFilter {

    private LocalDateTime minStartTime;

    private LocalDateTime maxStartTime;
}
