package com.brandnewdata.mop.poc.process.parser.dto;

import lombok.Data;

@Data
public class ShapeCenter {

    long[] geometryCenter;

    long[] upCenter;

    long[] rightCenter;

    long[] downCenter;

    long[] leftCenter;
}
