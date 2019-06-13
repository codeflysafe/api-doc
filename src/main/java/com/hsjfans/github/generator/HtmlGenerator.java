package com.hsjfans.github.generator;


import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.*;
import com.hsjfans.github.util.CollectionUtil;
import com.hsjfans.github.util.FileUtil;
import com.hsjfans.github.util.StringUtil;

import java.util.List;


/**
 *
 * html 文档生成器
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class HtmlGenerator extends AbstractGenerator {


    private static final String BASE_TPL_PATH = "src/main/resources/tpl/";
    private static final String controllerTpl = "api-controller.html";
    private static final String Index = "index.html";
    private static final String urlTpl = "api-url.html";
    private static final String extraTpl = "js.html";

    private static final String Request_Params_Table_No_head = " \n <tr>\n" +
            "            <td>%s</td>\n" +
            "            <td>%s</td>\n" +
            "            <td>%s</td>\n" +
            "            <td>%s</td>\n" +
            "            <td>%s</td>\n" +
            "            <td>%s</td>\n" +
            "        </tr> ";
    private static final String Union_No_head = " \n <tr>\n" +
            "            <td>%s</td>\n" +
            "            <td>%s</td>\n" +
            "        </tr> ";

    private static final String Request_Params_Table_head = "<table class=\"table\">\n" +
            "        <tr align=\"center\">\n" +
            "            <th>名称</th>\n" +
            "            <th>类型</th>\n" +
            "            <th>取值</th>\n" +
            "            <th>必需</th>\n" +
            "            <th>模糊</th>\n" +
            "            <th>说明</th>\n" +
            "        </tr>\n" +
            "        ${requestParams}\n" +
            "    </table>";

    private static final String Response_Return_Table_Head = "<table class=\"table\">\n" +
            "        <!--<caption><h4></h4></caption>-->\n" +
            "        <tr align=\"center\">\n" +
            "            <th>名称</th>\n" +
            "            <th>类型</th>\n" +
            "            <th>取值</th>\n" +
            "            <th>说明</th>\n" +
            "        </tr>\n" +
            "        ${responses}\n" +
            "    </table>";


    private static final String Response_Return_Table_No_Head =
            " <tr>\n" +
                    "            <td>%s</td>\n" +
                    "            <td>%s</td>\n" +
                    "            <td>%s</td>\n" +
                    "            <td>%s</td>\n" +
                    "        </tr>";


    @Override
    public void from(ApiTree apiTree, Config config) {
        this.config = config;
        buildExtraDoc();
        StringBuilder controllerList = new StringBuilder();
        apiTree.getSet().forEach(
                controllerClass -> {
                    controllerList.append(String.format("\n <li style=\"padding:5px\" > <a href=\"%s\" >%s</a> <span> %s </span>  </li>"
                    ,"./"+controllerClass.getName()+".html",controllerClass.getName(),controllerClass.getDescription()));

                    // 开始构建的 controller 文件
                    buildControllerDoc(controllerClass);
                }
        );
        String indexHtml = FileUtil.from(BASE_TPL_PATH+Index);
        indexHtml =  indexHtml.replace("${api-doc-description}",config.getDocName());
        indexHtml =  indexHtml.replace("${api-doc-name}",config.getDocName());
        indexHtml = indexHtml.replace("${api-controller-item}",controllerList.toString());
        indexHtml = indexHtml.replace("${count}",String.valueOf(apiTree.getSet().size()));
        FileUtil.to(this.config.getOutPath()+"index.html",indexHtml);

    }


    /**
     *  构建 controllerClass 文件
     * @param controllerClass controller
     */
    @Override
    protected void buildControllerDoc(ControllerClass controllerClass) {

        System.out.println(controllerClass);
        StringBuilder controllerHtml = new StringBuilder();
        String controller = FileUtil.from(BASE_TPL_PATH+controllerTpl);
        controller =  controller.replace("${controller-name}",controllerClass.getName());
        controller =  controller.replace("${count}",String.valueOf(controllerClass.getControllerMethod().size()));
        controller =  controller.replace("${controller-description}",controllerClass.getDescription());
        controller =  controller.replace("${author}",controllerClass.getAuthor());
        controller =  controller.replace("${baseUrl}", StringUtil.join(controllerClass.getUrl(),","));
        controllerClass.getControllerMethod().forEach(controllerMethod -> {
            // 填充 列表
            controllerHtml.append(String.format("\n <li style=\"padding:5px\"> <a href=\"%s\" >%s</a> <span> %s </span> </li>"
                    ,"./"+controllerClass.getName()+"_"+controllerMethod.getName()+".html",controllerMethod.getName(),controllerMethod.getName()));

            // 构建 api 详情
            buildApiDoc(controllerClass,controllerMethod);
        });

        controller =  controller.replace("${controller-methods}",controllerHtml.toString());

        // 生成文件
        FileUtil.to(this.config.getOutPath()+controllerClass.getName()+".html",controller);
    }


    /**
     *  构建详细的 api 文件
     * @param controllerClass
     * @param controllerMethod
     */
    @Override
    protected void buildApiDoc(ControllerClass controllerClass, ControllerMethod controllerMethod) {

        System.out.println(controllerClass);

        String method = FileUtil.from(BASE_TPL_PATH+urlTpl);
        method = method.replace("${title}",controllerMethod.getName());
        method = method.replace("${api-url-name}",controllerMethod.getName());
        method = method.replace("${prev-name}",controllerClass.getName());
        method = method.replace("${prev-url}",controllerClass.getName()+".html");
        if(controllerClass.getUrl().length==0){
            controllerClass.setUrl(new String[]{""});
        } else if(controllerMethod.getUrl().length==0){
            controllerMethod.setUrl(new String[]{""});
        }

        String[] urls = new String[controllerClass.getUrl().length*controllerMethod.getUrl().length];
        int i=0;
        for(String baseUrl:controllerClass.getUrl()){
            for(String url:controllerMethod.getUrl()){
               urls[i++] = baseUrl+url;
            }
        }
        method = method.replace("${urls}",StringUtil.join(urls,","));
        method = method.replace("${api-url-description}",controllerMethod.getDescription());
        method = method.replace("${methods}", CollectionUtil.requestMethodsToString(controllerMethod.getMethods()));
        method = method.replace("${author}",controllerMethod.getAuthor());
        method = method.replace("${requestParams}", generateRequestParams(controllerMethod.getRequestParameters()));
        method = method.replace("${responses}", generateResponseReturn(controllerMethod.getResponseReturn()));


        FileUtil.to(this.config.getOutPath()+controllerClass.getName()+"_"+controllerMethod.getName()+".html",method);
    }


    /**
     *  构建请请求参数 页面
     * @param requestParams requestParams
     * @return
     */
    private String generateRequestParams(List<RequestParameter> requestParams){
        StringBuilder params = new StringBuilder();
        requestParams.forEach(requestParam->{
            System.out.println(" requestParam= "+requestParam);
            if(requestParam.getFields()==null||requestParam.getFields()!=null&&requestParam.getEnumValues()!=null){
                params.append(String.format(Request_Params_Table_No_head,requestParam.getName(),requestParam.getTypeName(),
                        StringUtil.enumToStrs(requestParam.getEnumValues()),!requestParam.isNullable(),requestParam.isFuzzy(),
                        requestParam.getDescription()));
            }else {
                params.append(String.format(Request_Params_Table_No_head,
                        requestParam.getName(),
                        Request_Params_Table_head.replace("${requestParams}",generateClassFields(requestParam.getFields(),false)),
                        StringUtil.enumToStrs(requestParam.getEnumValues()),!requestParam.isNullable(),requestParam.isFuzzy(),
                        requestParam.getDescription()
                ));
            }

        });

        return params.toString();

    }


    /**
     *  构建返回值 页面
     * @param responseReturn
     * @return
     */
    private String generateResponseReturn(ResponseReturn responseReturn){

        StringBuilder responses = new StringBuilder();

        if(responseReturn.getReturnItem()!=null&&responseReturn.getReturnItem().size()>0){
            responses.append(String.format(Response_Return_Table_No_Head,
                    responseReturn.getName(),
                    Response_Return_Table_Head.replace("${responses}",generateClassFields(responseReturn.getReturnItem(),true)),
                    StringUtil.enumToStrs(responseReturn.getEnumValues()),
                    responseReturn.getDescription()
            ));
        }else {
            responses.append(String.format(Response_Return_Table_No_Head,
                    responseReturn.getName(),
                    responseReturn.getType(),
                    StringUtil.enumToStrs(responseReturn.getEnumValues()),
                    responseReturn.getDescription()));
        }
        return responses.toString();
    }


    /**
     *  构建 classFields 文件
     * @param classFields classFileds
     * @return
     */
    private String generateClassFields(List<ClassField> classFields,boolean response){

        StringBuilder params = new StringBuilder();
        classFields.forEach(classField->{
            if(classField.getFields()==null||classField.getFields()!=null&&classField.getEnumValues()!=null){
                if(response){
                    params.append(String.format(Response_Return_Table_No_Head,
                            classField.getName(),
                            classField.getType(),
                            StringUtil.enumToStrs(classField.getEnumValues()),
                            classField.getDescription()));
                }else {
                    params.append(String.format(Request_Params_Table_No_head,
                            classField.getName(),
                            classField.getType(),
                            StringUtil.enumToStrs(classField.getEnumValues()),
                            !classField.isNullable(),classField.isFuzzy(),
                            classField.getDescription()));
                }

            }else {
                if(response){
                    params.append(String.format(Response_Return_Table_No_Head,
                            classField.getName(),
                            Response_Return_Table_Head.replace("${responses}",generateClassFields(classField.getFields(),true)),
                            StringUtil.enumToStrs(classField.getEnumValues()),
                            classField.getDescription()
                    ));
                }else {
                    params.append(String.format(Request_Params_Table_No_head,
                            classField.getName(),
                            Request_Params_Table_head.replace("${requestParams}",generateClassFields(classField.getFields(),false)),
                            StringUtil.enumToStrs(classField.getEnumValues()),!classField.isNullable(),classField.isFuzzy(),
                            classField.getDescription()
                    ));
                }
            }

        });

        return params.toString();

    }







    @Override
    protected void buildExtraDoc() {
        String extra = FileUtil.from(BASE_TPL_PATH+extraTpl);
        FileUtil.to(this.config.getOutPath()+extraTpl,extra);
    }
}
