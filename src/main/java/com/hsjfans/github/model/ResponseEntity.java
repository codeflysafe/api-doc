package com.hsjfans.github.model;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * api return instance
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class ResponseEntity implements Serializable {

    private List<Field> fields;



}
