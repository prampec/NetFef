/*
 * This file is part of the NetFef serial network bus protocol project.
 *
 * Copyright (c) 2015.
 * Author: Balazs Kelemen
 * Contact: prampec+netfef@gmail.com
 *
 * This product is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) license.
 * Please contact the author for a special agreement in case you want to use this creation for commercial purposes!
 */

package com.netfef.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Complex data structure</p>
 * <p>User: kelemenb
 * <br/>Date: 5/11/15</p>
 */
public class Struct {
    private Map<Character, List<Parameter>> parameters = new HashMap<>();

    public Struct() {
    }

    public Struct(List<Parameter> parameters) {
        this.setParameters(parameters);
    }

    public List<Parameter> getParameters() {
        List<Parameter> retVal = new ArrayList<>();
        for (List<Parameter> parameterList : parameters.values()) {
            retVal.addAll(parameterList);
        }
        return retVal;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Parameter parameter : getParameters()) {
            if(first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(parameter.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    public void addParameter(Parameter parameter) {
        char parameterName = parameter.getParameterName();
        List<Parameter> list;
        if (parameters.containsKey(parameterName)) {
            list = parameters.get(parameterName);
        }
        else {
            list = new ArrayList<>();
            parameters.put(parameterName, list);
        }
        list.add(parameter);
    }

    public void setParameters(List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            this.addParameter(parameter);
        }
    }

    public boolean hasParameter(char paramName) {
        return this.parameters.containsKey(paramName);
    }

    public Parameter getParameter(char parameterName) {
        List<Parameter> list = parameters.get(parameterName);
        return list == null ? null : list.get(0);
    }
    public List<Parameter> getParameterList(char parameterName) {
        return parameters.get(parameterName);
    }

}
