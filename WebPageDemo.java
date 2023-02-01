package platform.demo;

import platform.demo.dto.Breadcrumb;
import platform.demo.dto.WebDAVRes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



import com.planonsoftware.platform.data.v1.BusinessException;
import com.planonsoftware.platform.webdav.v1.IWebDAVResource;
import com.planonsoftware.platform.webpage.v9.IWebPage;
import com.planonsoftware.platform.webpage.v9.IWebPageContext;
import com.planonsoftware.platform.webpage.v9.common.WebPagePath;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.AjaxFileDropBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IResourceStreamWriter;
import org.apache.wicket.util.string.StringValue;



import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.resource.ZipResourceStream;



import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.markup.html.form.TextField;


@WebPagePath("webpagedemo")
public class WebPageDemo extends WebPage implements IWebPage {

    private static final long serialVersionUID = 1L;
    private ListView<WebDAVRes> webDAVResListView;
    private List<WebDAVRes> webDAVResList;
    private FileUploadField fileUpload;
    private String currentPath;
    private String[] breadcrumb;
    private List<Breadcrumb> breadcrumbL;
    private List<String> breadcrumbS;
    private TextField nameField;
    private Check listBox;
    
    
    private ArrayList<WebDAVRes> array;
    private List<File> filesToBeWritten = new ArrayList<File>();

    private List<IResource> resources = new ArrayList<>();
    private String nameDownload;
    private List<File> files = new ArrayList<File>();


    /**BREADCRUMB**/
    public WebPageDemo(PageParameters parameters) throws IOException, BusinessException, URISyntaxException {
        super(parameters);
       
        StringValue stringValuePath = parameters.get("ruta");
        if (stringValuePath.isNull() || stringValuePath.toString().isEmpty()){
            setRelativePath("/webdav"); //MAIN
        } else{
            setRelativePath(stringValuePath.toString());
            breadcrumb = stringValuePath.toString().split("/");
            breadcrumbS = new ArrayList<>(Arrays.asList(breadcrumb));
            breadcrumbS.remove(0); //Eliminamos el primer elemento porque el primer split pilla un elemento vacío
            String rutaRelativa = "";
            breadcrumbL = new ArrayList<Breadcrumb>();
                for(String name: breadcrumbS){
                    rutaRelativa = rutaRelativa +"/" + name;
                    breadcrumbL.add(new Breadcrumb(name, rutaRelativa)); 
                }
            } 
        }

        /**********/
        

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getContext().getLogService().info("------------> Inicializando Demo ************************" );


         final WebMarkupContainer menuContainer = new WebMarkupContainer("menu");
            menuContainer.setOutputMarkupId(true);  
            
         //Botón upload --> ocultar o visualizar subida de archivos
        Link<Void> uploadButtonLink = new Link<Void>("uploadButton") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                
            }
        };
        menuContainer.add(uploadButtonLink);
        add(menuContainer);
        // uploadButtonLink.setOutputMarkupId(true);


        final WebMarkupContainer container = new WebMarkupContainer("container");
            container.setOutputMarkupId(true);

        try {
            info(getRelativePath() == null ? "null" : getRelativePath());
            String path = getMainURL()+getRelativePath();
            URI uriPathWebdav = new URI(path);
            //webdavMain = getContext().getWebDAVService().getWebDAVResource(uriPathWebdav);
            IWebDAVResource webdavMain = getContext().getWebDAVService().getWebDAVResource(uriPathWebdav);
            getContext().getLogService().info("------------>" + uriPathWebdav);

            long startTime = System.nanoTime();
                List<IWebDAVResource>  listResources = webdavMain.getChildResources();
            long endTime = System.nanoTime() - startTime;//Tiempo ejecución del método 
            getContext().getLogService().info("-------------> Tiempo de carga de elementos getChildResources(): " + endTime);



            getContext().getLogService().info("-------------> Lista size" +listResources.size());
            webDAVResList = Utils.getWebDAVResList(listResources); 
        } catch (IOException | BusinessException e) {         
            getContext().getLogService().error("-------------> Error: en getWebDAVResource o getChildResources");
            info("Error al obtener recursos. Por favor, inténtelo más tarde.");
            e.printStackTrace();
            e.getCause();
        } catch (URISyntaxException e) {
            getContext().getLogService().error("-------------> Error: URI Incorrecta");
            e.printStackTrace();
        }

    //////////////////////////////////PANEL CARGA DE FICHEROS    
        /*************************PANEL CARGA FICHEROS************************/
        // ## Form
        Form<?> form = new Form<Void>("form");
        // ## Button SUBMIT
        Button buttonSubmit = new Button("buttonSubmit") {
             
            private static final long serialVersionUID = 1L;

            @Override
			public void onSubmit() {
                final List<FileUpload> uploadedFiles = fileUpload.getFileUploads();
               
                getContext().getLogService().info("-----TAMAÑO DE LISTA (upload)-------> " + uploadedFiles.size());
                getContext().getLogService().info("-----EMPTY?-------> " + uploadedFiles.isEmpty());
                Iterator<FileUpload> iter = uploadedFiles.iterator();       //Iterador para recorrer la lista de archivos subidos
                 
                while(iter.hasNext()){
                    FileUpload fileUp = iter.next();

                    if(fileUp != null){
                        try {
                            String fileName = fileUp.getClientFileName();    
                            InputStream is = fileUp.getInputStream();                               
                            URI directoryURI = new URI(getMainURL() +getRelativePath()); //"/webdav/Documents/Test"
                            IWebDAVResource directory = getResource(directoryURI);
                            IWebDAVResource file = directory.getChildResource(fileName);
                            file.put(is);
                            is.close();
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace(); 
                            getContext().getLogService().error("-----CATCH-------> " + e.toString());
                        }
                        
                    }
                }

                /*Para recargar la página*/
                PageParameters pageParameters = new PageParameters();
                pageParameters.add("ruta", getRelativePath());
                setResponsePage(WebPageDemo.class, pageParameters);
                /****FIN RECARGA PÁGINA************/
            }
            
            @Override
            public void onError()
            {
                getContext().getLogService().error("-----Error onSubmit()-------> ");
            }
		};
		
		// Enable multipart mode (need for uploads file)
		form.setMultiPart(true);

		// max upload size, 10k
		form.setMaxSize(Bytes.kilobytes(10000));
 
        form.add(fileUpload = new FileUploadField("fileUpload")); //Add button EXAMINE to form
        form.add(buttonSubmit); //Add button SUBMIT to form

///////////////////////////////////////DRAG AND DROP//////////////////////////////  
        WebMarkupContainer drop = new WebMarkupContainer("drop");
        
        drop.add(
            new AjaxFileDropBehavior(){
                private static final long serialVersionUID = 1L;
                    
                protected void onFileUpload(AjaxRequestTarget target, List<FileUpload> files){
                    long startTime = System.nanoTime();

                    if (files == null || files.isEmpty()){
                        info("No file upload");
                    }else{
                        getContext().getLogService().info("-----TAMAÑO DE LISTA (DragDrop)-------> " + files.size());
                        for (FileUpload file: files){
                            info("Name: " + file.getClientFileName());
                            try {
                                InputStream is = file.getInputStream();
                                URI directoryURI = new URI(getMainURL() +getRelativePath());
                                IWebDAVResource directory = getResource(directoryURI);
                                IWebDAVResource fi = directory.getChildResource(file.getClientFileName());
                                fi.put(is);
                                is.close();

                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                                error("ErrorE: " + e.toString());
                            }
                        }
                        PageParameters pageParameters = new PageParameters();
                        pageParameters.add("ruta",getRelativePath());
                        setResponsePage(WebPageDemo.class, pageParameters);
                    }


                    long endTime = System.nanoTime() - startTime;//Tiempo ejecución del método 
                    getContext().getLogService().info("-------------> Tiempo subida de archivos (Drag&Drop): " + endTime);
                }

                @Override
                protected void onError(AjaxRequestTarget target, FileUploadException fux){
                    info(fux.getMessage());
                    // target.add(feedback);
                    getContext().getLogService().error("-----DragDrop ERROR getMessage()-------> " +  fux.getMessage() );
                    getContext().getLogService().error("-----DragDrop ERROR getLocalizedMessage()-------> " +  fux.getLocalizedMessage() );
                }

            }
        );
        form.add(drop);

    /////////////////////////////////////////////////////////////////////////////////7777

        container.add(form); // Add form to CONTAINER
        
        //Botón reload
        Link<Void> reloadButton = new Link<Void>("reload") {
        private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                PageParameters pageParameters = new PageParameters();
                pageParameters.add("ruta","/webdav/Documents");
                setResponsePage(WebPageDemo.class, pageParameters);
            }
        };

        container.add(reloadButton);


        //Añadir breadcrumb
        ListView<Breadcrumb> breadCrumbListView = new ListView<Breadcrumb>("breadcrumbList", breadcrumbL) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final ListItem<Breadcrumb> element) {

                Link<Void> breadcrumb = new Link<Void>("breadcrumbLinkSelect") {
                                   
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick() {
                        PageParameters pageParameters = new PageParameters();
                        pageParameters.add("ruta", element.getModel().getObject().getRelative());
                        setResponsePage(WebPageDemo.class, pageParameters); 
                    }
                };
                
                breadcrumb.add(new Label("spanLabel", element.getModel().getObject().getName()));

                element.add(breadcrumb);

            }
         };

        //Breadcrumb
        container.add(breadCrumbListView);


        /** CHECKBOX AND DOWNLOAD **/
        final CheckGroup<WebDAVRes> group = new CheckGroup<>("group", array = new ArrayList<>());
        Form <Void> form2 = new Form<Void>("form2");
        
        Button submitDownload = new Button("submitDownload") { 
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit(){
 
                
                    info("ANTES DE DESCARGAR");
                    info("array:" +array.size());
                    if(array.size() == 1){
                        for(WebDAVRes iter: array){
                            
                            nameDownload = iter.getName();

                                AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter(){
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void write(OutputStream output) throws IOException {
                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();

                                    IWebDAVResource webdavResource;
                                    try{
                                        webdavResource = getResource(new URI(getMainURL()+ getRelativePath()+"/"+(nameDownload.replace(" ", "%20"))));
                                        webdavResource.get().transferTo(bao);
                                        output.write(bao.toByteArray());
                                    }catch(URISyntaxException e){
                                        getContext().getLogService().error("-------------> No se pudo descargar el archivo");
                                        e.printStackTrace();
                                    }
                                }

                            };
                                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, (nameDownload));
                                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
                        }
                    }else{
                       

                            IWebDAVResource webdavResource;
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ZipOutputStream zos = new ZipOutputStream(baos);
                            for(WebDAVRes iter: array){
                                info("ARRAY:" +array);
                                filesToBeWritten.add(new File(iter.getName()));
                            }
                                try {  
                                    for(File file: filesToBeWritten){
                                        info(filesToBeWritten.size());
                                        webdavResource = getResource(new URI(getMainURL()+ getRelativePath()+"/"+(file.getName().replace(" ", "%20"))));
                                        InputStream fileContent = webdavResource.get();
                                        zos.putNextEntry(new ZipEntry(file.getName()));
                                        byte[] buffer = new byte[2048];

                                        int len;
                                        while((len = fileContent.read(buffer)) > 0){
                                            zos.write(buffer,0,len);
                                        }
                                        zos.closeEntry();
                                        fileContent.close();
                                    }
                                    zos.close();
                                    baos.close();
                                    

                                    if(filesToBeWritten.size() > 1){
                                        WebResponse response = (WebResponse) getRequestCycle().getResponse();
                                        response.setContentType("application/zip");
                                        response.setHeader("Content-Disposition", "attachment; filename=myZip.zip");
                                        response.setContentLength(baos.size());
                                        response.write(baos.toByteArray());
                                    }else{
                                        info("NO HA PILLADO TODO");
                                    }    

                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    // }
                
                }    

            }
        };

        

        container.add(form2);
        form2.add(group);
        group.add(new CheckGroupSelector("groupselector")); 
        form2.add(submitDownload);
        /** END CHECKBOX**/
        



         /** CREATE A NEW FOLDER **/
        Form <Void> formCreateFolder = new Form<Void>("formCreateFolder");
        Button CreateFolder = new Button("newFolderButton"){

            private static final long serialVersionUID = 1L;
            

            @Override
            public void onSubmit(){ 
                info("HOLAA");
                info("NAME FIELD:" +nameField.getValue().toString());
                                
                try{
                    String nameFolder = nameField.getValue().toString();
                    IWebDAVResource name;

                    String path = getMainURL()+getRelativePath();
                    URI uriPathWebdav = new URI(path);
                    IWebDAVResource webdavMain = getContext().getWebDAVService().getWebDAVResource(uriPathWebdav);
                    List<IWebDAVResource>  listResources = webdavMain.getChildResources();
                    info("antes del while");
                    Iterator<IWebDAVResource> iter = listResources.iterator(); 

                    while(iter.hasNext()){
                        name = iter.next();
                        info("name:" +name);
                    
                        if (name.getName().equals(nameFolder)){
                            if(name.isDirectory()){
                                info("No se puede crear el directorio");
                                
                            }else{
                                webdavMain.createDirectory(nameFolder);
                                info("Directorio creado");
                            }
                        }else{
                            webdavMain.createDirectory(nameFolder);
                            info("Directorio creado");
                        }
                    }
                }catch(IOException | BusinessException | URISyntaxException e){
                     e.printStackTrace();
                }

                PageParameters pageParameters = new PageParameters();
                pageParameters.add("ruta", getRelativePath());
                setResponsePage(WebPageDemo.class, pageParameters);
                
            }
        };
        menuContainer.add(formCreateFolder);
        formCreateFolder.add(CreateFolder);
        formCreateFolder.setDefaultModel(new CompoundPropertyModel<>(this));
        formCreateFolder.add(nameField = new TextField<>(("text"), Model.of("")));
        /**END CREATE A NEW FOLDER **/



        /*************************LIST OF OBJECT (TABLE) ************************/
        long startTime = System.nanoTime(); 
        webDAVResListView = new ListView<WebDAVRes>("webDAVRess", webDAVResList) {
            private static final long serialVersionUID = 1L;

           

            @Override
            protected void populateItem(final ListItem<WebDAVRes> item) {

                WebDAVRes object = item.getModel().getObject();
                String objectName = item.getModel().getObject().getName();     
                item.add(new Check<>("checkbox", item.getModel())); 
                
                // item.add(listBox = new Check<>("checkbox"));
                
                // String nameListBox = ((WebDAVRes) listBox.getModel().getObject()).getName();
                // info("NAME LIST BOX: " +nameListBox);
                

                Link<Void> carpetaLink = new Link<Void>("externalSite2") {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick() {
                        PageParameters pageParameters = new PageParameters();
                        if(object.isDirectory()){
                            pageParameters.add("ruta",getRelativePath()+"/"+objectName);
                            setResponsePage(WebPageDemo.class, pageParameters);
                        }
                        
                    }
                };

                /**Icono carpeta o tipo de archivo */
        
            
                WebMarkupContainer markupICono = new WebMarkupContainer("iconoImage");
                if(item.getModel().getObject().isDirectory()){ 
                    markupICono.add(new AttributeModifier( "src", new Model<String>("./webpagedemo/wicket/resource/platform.demo.WebPageDemo/::/demo/icons/folder.png"))); 
                } else{//Si es archivo no tiene que poder abrir el link (Navegación entre carpetas)
                    if(object.getContentType().equals("application/pdf"))
                         markupICono.add( new AttributeModifier( "src", new Model<String>("./webpagedemo/wicket/resource/platform.demo.WebPageDemo/::/demo/icons/pdf.png")));
                    else if(object.getContentType().contains("image/"))
                         markupICono.add( new AttributeModifier( "src", new Model<String>("./webpagedemo/wicket/resource/platform.demo.WebPageDemo/::/demo/icons/image.png")));
                    else
                        markupICono.add( new AttributeModifier( "src", new Model<String>("./webpagedemo/wicket/resource/platform.demo.WebPageDemo/::/demo/icons/google-docs.png"))); 
                }
                /**FIN Icono carpeta o tipo de archivo */
            
                carpetaLink.add(markupICono); //Añadimos imagen al enlace
                item.add(carpetaLink); //Añadimos "imagen con enlace"
           
                item.add(new Label("webdavObjectName", new PropertyModel<String>(item.getModel(), "name"))
                        .setOutputMarkupId(true));
                item.add(new Label("webdavModifiedDate", new PropertyModel<String>(item.getModel(), "modifiedDate"))
                        .setOutputMarkupId(true));
                item.add(new Label("webdavSize", new PropertyModel<String>(item.getModel(), "size"))
                .setOutputMarkupId(true));

                
                //Botón download
                Link<Void> downloadButtonLink = new Link<Void>("downloadButton") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                     
                         String webdavName = item.getModelObject().getName(); //item.getModel().getObject().getName(); 
                        
                        getContext().getLogService().info("-------------> Archivo que queremos descargar" + webdavName);

                        AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void write(OutputStream output) throws IOException {
                                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                
                                IWebDAVResource webdavResource;
                                try {
                                    webdavResource = getResource(new URI(getMainURL()+ getRelativePath()+"/"+webdavName.replace(" ", "%20")));  // "/webdav/Documents/Test/" //debe tener / al final
                                    webdavResource.get().transferTo(bao);
                                    output.write(bao.toByteArray());
                                } catch (URISyntaxException e) {
                                    getContext().getLogService().error("-------------> No se pudo descargar el archivo");
                                    e.printStackTrace();
                                }
                                
                            }
                        };

                        ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, webdavName);
                        getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
                    }
                };
                
                // downloadButtonLink.setOutputMarkupId(true);
               // menuContainer.add(downloadButtonLink); //Añadir botón download a la vista      
                
                   
            }
        };
        long endTime = System.nanoTime() - startTime;//Tiempo ejecución del método 
        getContext().getLogService().info("-------------> Tiempo de carga de elementos (PopulateItem): " + endTime);

        webDAVResListView.setOutputMarkupId(true);
        container.add(webDAVResListView);
        add(container);

        /** CHECKBOX **/
        webDAVResListView.setReuseItems(true);
        group.add(webDAVResListView); 
        container.add(new FeedbackPanel("feedback")); 
        
        

    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new CssResourceReference(WebPageDemo.class, "style.css")));
    }

    @Override
    public void setContext(final IModel<IWebPageContext> aContext) {
        setDefaultModel(aContext);
    }

    IWebPageContext getContext() {
        return (IWebPageContext) getDefaultModelObject();
    }

   private String getMainURL(){    
       return "http://tomcat-webdav:8081";
   } 

    private IWebDAVResource getResource(URI absoluteResourcePath){
        IWebDAVResource resource = null;
        try {
             resource = getContext().getWebDAVService().getWebDAVResource(absoluteResourcePath);
            
        } catch (BusinessException |IOException  e) {
            getContext().getLogService().error("------Exception in method getResource------>" + e.toString());
        }
        return resource;
    }


    private String getRelativePath(){
       return currentPath;
   }

   private void setRelativePath(String path){
       currentPath = path;
   }


} //END
