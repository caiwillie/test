package com.brandnewdata.mop.poc.process.dto;

import lombok.Data;

@Data
public class BoundXY {

    long[] center;

    long[] up;

    long[] right;

    long[] down;

    long[] left;
}
