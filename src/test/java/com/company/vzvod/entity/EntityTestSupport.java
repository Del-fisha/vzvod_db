package com.company.vzvod.entity;

import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EntityTestSupport {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Validator validator;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected DatatypeFormatter datatypeFormatter;
}
