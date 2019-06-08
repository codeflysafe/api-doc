package com.hsjfans.github.model;

import com.hsjfans.github.util.StringUtil;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class ControllerMethod implements Serializable {


    /**
     *  is true will be ignored
     */
    private boolean ignore;

    /**
     *
     *  the method url
     */
    private String[] url ;

    /**
     *  the method name `@name`
     **/
    private String name;


    /**
     *  the method
     */
    private Method method;


    /**
     *  the class
     */
    private Class<?> aClass;

    /**
     *  the args
     */
    private RequestParam[] params;


    /**
     *  the responseReturn
     */
    private ResponseReturn responseReturn;


    /**
     *  the request that api support
     */
    private RequestMethod[] methods;


    public void addRequestMethod(RequestMethod method){
        this.methods = new RequestMethod[]{method};
    }


    public void setRequestMethods(String methods){

        for (String m: StringUtil.parseUrls(methods)
             ) {
           RequestMethod method = RequestMethod.valueOf(m.trim());
           if(method!=null){
//               this
           }
        }
    }


}
