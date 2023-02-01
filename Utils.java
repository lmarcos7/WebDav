package platform.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.planonsoftware.platform.data.v1.IActionListManager;
import com.planonsoftware.platform.data.v1.IBusinessObject;
import com.planonsoftware.platform.webdav.v1.IWebDAVResource;
import com.planonsoftware.platform.data.v1.AuthorizationException;
import com.planonsoftware.platform.data.v1.BusinessException;
import com.planonsoftware.platform.data.v1.IAction;
import com.planonsoftware.platform.data.v1.IDatabaseQuery;
import com.planonsoftware.platform.data.v1.IResultSet;
import com.planonsoftware.platform.data.v1.IStateChange;
import com.planonsoftware.platform.data.v1.Operator;
import com.planonsoftware.platform.webpage.v9.IWebPageContext;

import org.apache.commons.io.IOUtils;

import platform.demo.dto.Asset;
import platform.demo.dto.WebDAVRes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    static long kilo = 1024;
    static long mega = kilo * kilo;
    static long giga = mega * kilo;
    static long tera = giga * kilo;

    public static List<Asset> getPersonAssets(final IWebPageContext context) {
        List<Asset> personAssets = new ArrayList<Asset>();

        try {
            IDatabaseQuery personAssetsQuery = context.getDataService().getPVDatabaseQuery("GetAssetsQuery");
            personAssetsQuery.getReferenceSearchExpression("PersonRef", Operator.EQUAL)
                    .addValue(context.getUserService().getPersonPrimaryKey());
            personAssetsQuery.getSearchExpression("AssetStatePnName", Operator.NOT_EQUAL).addValue("UsrBaseAssetDisposedOf");
            IResultSet queryResults = personAssetsQuery.execute();

            while (queryResults.next()) {
                Asset asset = new Asset(queryResults.getPrimaryKey(), queryResults.getString("Code"),
                        queryResults.getString("Name"), queryResults.getString("Brand"),
                        queryResults.getString("AssetState"));
                personAssets.add(asset);
            }

        } catch (BusinessException | AuthorizationException e) {
            e.printStackTrace();
        }

        return personAssets;
    }

    public static List<WebDAVRes> getWebDAVResList(List<IWebDAVResource> list) throws IOException {
        List<WebDAVRes> newList = new ArrayList<WebDAVRes>();
        for(IWebDAVResource res: list){
            newList.add(new WebDAVRes(res.getName(), /*res.getContentType()*/ nameExtension(res.getName(), res.getContentType()), 
            conversorLongToSimpleDateFormat(res.getModifiedDate()), 
            res.isFile(), res.isDirectory(), getSize(res.get()))); //Añade WebDAVRes a la lista
        }
        return newList; //Retorna lista de WebDAVRes
    }

    public static String conversorLongToSimpleDateFormat (Long modifiedDate){
        String newModifiedDate;
        if(modifiedDate == 0L) //Si es 0L -->es una carpeta
            newModifiedDate ="";
        else{

            Date dateX = new Date(modifiedDate);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
            newModifiedDate = df.format(dateX);

        


            // Date date = new Date(modifiedDate); //
            // SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            // newModifiedDate = dateFormat.format(date);
        }
        return newModifiedDate;
    }

    public static String nameExtension (String name, String contentType){
        String extension = contentType;
        if(contentType.length() > 30){
            String[] splitName = name.split("\\."); //prueba.docx
            String[] splitContentType = contentType.split("/"); //application/document...
            extension =  splitContentType[0] +"/"+ splitName[1] ; //application/docx
        }
        return extension;
    }

    public static String getTypeFileIcon (Boolean isFile, String contentType){
        String typeFile;
        if(isFile){ //documento cualquiera
            if (contentType.equals("application/pdf")){
                typeFile = "pdf";
            }else if (contentType.contains("image/")){
                typeFile = "image";
            }else{
                typeFile = "other";
            }
            
        } else { //carpeta
            typeFile = "folder";
        }
        return typeFile;
    }

    public static void disposeAsset(IWebPageContext context, int aAssetSyscode) {
        try {
            IActionListManager actionListManager = context.getDataService().getActionListManager("BaseAsset");
            IAction readAction = actionListManager.getReadAction(aAssetSyscode);
            IBusinessObject asset = readAction.execute();
            IStateChange disposeStateChange = asset.getStateChange("UsrBaseAssetDisposedOf");
            asset = asset.executeStateChange(disposeStateChange);
            asset.executeSave();
         } catch (BusinessException | AuthorizationException e) {
            e.printStackTrace();
        }
    }

    public static String getPersonName(IWebPageContext aContext) {
        String name = "";

        try {
            IActionListManager actionListManager = aContext.getDataService().getActionListManager("Person");
            IAction readAction = actionListManager.getReadAction(aContext.getUserService().getPersonPrimaryKey());
            IBusinessObject person = readAction.execute();
            name = person.getStringField("FirstName").getValue();
        } catch (BusinessException | AuthorizationException e) {
            e.printStackTrace();
        }
        return name;
    }


     private static String getSize(InputStream is) {
        File file = new File("filename.tmp");
        try(OutputStream outputStream = new FileOutputStream(file)){
            IOUtils.copy(is, outputStream);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        long size = file.length();
        String s = "";
        double kb = (double)size / kilo;
        double mb = kb / kilo;
        double gb = mb / kilo;
        double tb = gb / kilo;
        if(size < kilo) {
            s = size + " Bytes";
        } else if(size >= kilo && size < mega) {
            s =  String.format("%.2f", kb) + " KB";
        } else if(size >= mega && size < giga) {
            s = String.format("%.2f", mb) + " MB";
        } else if(size >= giga && size < tera) {
            s = String.format("%.2f", gb) + " GB";
        } else if(size >= tera) {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
    }

}
