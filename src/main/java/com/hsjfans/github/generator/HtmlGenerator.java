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


    @Override
    protected void buildControllerDoc(ControllerClass controllerClass) {

        StringBuilder controllerHtml = new StringBuilder();
        String controller = FileUtil.from(BASE_TPL_PATH+controllerTpl);
//        controller =  controller.replace("${api-url-description}",controllerClass.getName());
        controller =  controller.replace("${controller-name}",controllerClass.getName());
        controller =  controller.replace("${count}",String.valueOf(controllerClass.getControllerMethod().size()));
        controller =  controller.replace("${controller-description}",controllerClass.getDescription());
        controller =  controller.replace("${author}",controllerClass.getAuthor());
        controller =  controller.replace("${baseUrl}", StringUtil.join(controllerClass.getUrl(),","));
        controllerClass.getControllerMethod().forEach(controllerMethod -> {

//            System.out.println( controllerMethod );

            controllerHtml.append(String.format("\n <li style=\"padding:5px\"> <a href=\"%s\" >%s</a> <span> %s </span> </li>"
                    ,"./"+controllerClass.getName()+"_"+controllerMethod.getName()+".html",controllerMethod.getName(),controllerMethod.getName()));
            buildApiDoc(controllerClass,controllerMethod);
        });
        controller =  controller.replace("${controller-methods}",controllerHtml.toString());
        FileUtil.to(this.config.getOutPath()+controllerClass.getName()+".html",controller);
    }

    @Override
    protected void buildApiDoc(ControllerClass controllerClass, ControllerMethod controllerMethod) {

        String method = FileUtil.from(BASE_TPL_PATH+urlTpl);
        method = method.replace("${title}",controllerMethod.getName());
        method = method.replace("${api-url-name}",controllerMethod.getName());
        method = method.replace("${prev-name}",controllerClass.getName());
        method = method.replace("${prev-url}",controllerClass.getName()+".html");
        if(controllerClass.getUrl().length==0){
            controllerClass.setUrl(new String[]{""});
        }
        if(controllerMethod.getUrl().length==0){
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
        method = method.replace("${requestParams}", generateRequestParams(controllerMethod.getParams()));
        method = method.replace("${responses}", generateResponseReturn(controllerMethod.getResponseReturn()));


        FileUtil.to(this.config.getOutPath()+controllerClass.getName()+"_"+controllerMethod.getName()+".html",method);
    }


    private String generateRequestParams(List<RequestParam> requestParams){
        StringBuilder params = new StringBuilder();
        requestParams.forEach(requestParam->{
            if(requestParam.getParams()!=null&&requestParam.getParams().size()>0){
                params.append(String.format(Request_Params_Table_No_head,
                        requestParam.getName(),
                        Request_Params_Table_head.replace("${requestParams}",generateRequestParams(requestParam.getParams())),
                        StringUtil.enumToStrs(requestParam.getEnumValues()),requestParam.isNecessary(),requestParam.isFuzzy(),
                        requestParam.getDescription()
                        ));
            }else {
                params.append(String.format(Request_Params_Table_No_head,requestParam.getName(),requestParam.getType(),
                        StringUtil.enumToStrs(requestParam.getEnumValues()),requestParam.isNecessary(),requestParam.isFuzzy(),
                        requestParam.getDescription()));
            }
        });

        return params.toString();

    }

    private String generateResponseReturn(ResponseReturn responseReturn){

        StringBuilder responses = new StringBuilder();

        if(responseReturn.getReturnItem()!=null&&responseReturn.getReturnItem().size()>0){
            responses.append(String.format(Response_Return_Table_No_Head,
                    responseReturn.getName(),
                    Response_Return_Table_Head.replace("${responses}",generateResponseItems(responseReturn.getReturnItem())),
                    StringUtil.enumToStrs(responseReturn.getEnumValues()),
                    responseReturn.getDescription()
            ));
//            responses.append(Response_Return_Table_Head.replace("${responses}",generateResponseItems(responseReturn.getReturnItem())));
        }else {
            responses.append(String.format(Response_Return_Table_No_Head,
                    responseReturn.getName(),
                    responseReturn.getType(),
                    StringUtil.enumToStrs(responseReturn.getEnumValues()),
                    responseReturn.getDescription()));
        }
        return responses.toString();
    }

    private String generateResponseItems(List<RequestParam> requestParams){
        StringBuilder responses = new StringBuilder();
        requestParams.forEach(requestParam -> {
            if(requestParam.getParams()!=null&&requestParam.getParams().size()>0){
                responses.append(String.format(Response_Return_Table_No_Head,
                        requestParam.getName(),
                        Response_Return_Table_Head.replace("${responses}",generateResponseItems(requestParam.getParams())),
                        StringUtil.enumToStrs(requestParam.getEnumValues()),
                        requestParam.getDescription()
                ));
            }else {
                responses.append(String.format(Response_Return_Table_No_Head,
                        requestParam.getName(),
                        requestParam.getType(),
                        StringUtil.enumToStrs(requestParam.getEnumValues()),
                        requestParam.getDescription()));
            }
        });
        return responses.toString();
    }

    @Override
    protected void buildExtraDoc() {
        String extra = FileUtil.from(BASE_TPL_PATH+extraTpl);
        FileUtil.to(this.config.getOutPath()+extraTpl,extra);
    }
}
