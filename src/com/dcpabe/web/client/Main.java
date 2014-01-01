package com.dcpabe.web.client;

import gwt.ns.webworker.client.MessageEvent;
import gwt.ns.webworker.client.MessageHandler;
import gwt.ns.webworker.client.Worker;
import gwt.ns.webworker.client.WorkerFactory;
import gwt.ns.webworker.client.WorkerModuleDef;
import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters;

import java.util.LinkedList;

import org.vectomatic.arrays.ArrayBuffer;
import org.vectomatic.file.File;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.FileUploadExt;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.nkdata.gwt.streamer.client.impl.Base91Util;

public class Main implements EntryPoint, MessageHandler {
	
	FileUploadExt fileUpload;
	FileUploadExt fileUpload_GP;
	TextBox textBox_attr;
	TextArea textArea_log;
	Image spinner_image;
	TextBox textBox_pol;
	TextBox textBox_user_attr;
	ListBox comboBox_threadnum;
	FileUploadExt fileUpload_ak;
	FileUploadExt fileUpload_priv;
	ListBox comboBox_numAuthAttr;
	ListBox comboBox_URI_size;
	
	private Button btnEncrypt;
	private Button btnDecrypt;
	LinkedList<Integer> presch = new LinkedList<Integer>();
	
	Worker worker;
	
	// Worker module definition
    @WorkerModuleDef("com.dcpabe.web.SchedulingWorker")
    interface SchedulingWorkerFactory extends WorkerFactory { }
    
    @WorkerModuleDef("com.dcpabe.web.ChildWorker")
    interface ChildWorkerFactory extends WorkerFactory { }
    
    public static int numCores_max = 6;
	private static final int DELAY_BETWEEN_CHILDREN = 100;
	
	private enum DATA_TYPE{
		ARRAY_BUFFER,
		STRING
	}
    int numCores = 2;
	private Worker[] child_workers;
	
	private int URIsize[]={1*1024*1024, 2*1024*1024, 5*1024*1024, 10*1024*1024, 20*1024*1024};

	private void processFiles_Ak(File item) {
		final FileReader reader = new FileReader();
		reader.addLoadEndHandler(new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				if (reader.getError() == null) {
					ArrayBuffer ab = (ArrayBuffer) reader.getArrayBufferResult();
					ArrayBuffer ab2 = ab.slice(0);
					worker.transferMessage(SchedulingWorker.PROPAGATE_PK, ab);
					repeatingPost tmp = new repeatingPost(((Integer)SchedulingWorker.PROPAGATE_PK).toString(), 
							ab2, 
							DATA_TYPE.ARRAY_BUFFER, 
							SchedulingWorker.PROPAGATE_PK);
					Scheduler.get().scheduleFixedDelay(tmp, DELAY_BETWEEN_CHILDREN);
				}
			}
		});
		reader.readAsArrayBuffer(item);
	}
	
	private void processFiles_priv(File item) {
		final FileReader reader = new FileReader();
		reader.addLoadEndHandler(new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				if (reader.getError() == null) {
					ArrayBuffer ab = (ArrayBuffer) reader.getArrayBufferResult();
					worker.transferMessage(SchedulingWorker.LOAD_PRIVATE, ab);
				}
			}
		});
		reader.readAsArrayBuffer(item);
	}
	
	private void processFiles_curveparams(File item) {
		final FileReader reader = new FileReader();
		reader.addLoadEndHandler(new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				Window.alert("got it, preparing to populate");
				if (reader.getError() == null) {			
					DefaultCurveParameters cp = new DefaultCurveParameters();
					String CurveParameterResult = reader.getStringResult();
					cp.load(CurveParameterResult);
					worker_populate(CurveParameterResult);
				}
			}
		});
		reader.readAsText(item);
	}
	
	protected void worker_populate(String CurveParameterResult) {
		worker.postMessage(SchedulingWorker.POPULATE_PARAM,CurveParameterResult);
		spinner_image.setVisible(true);
	}
	
	protected void worker_populate_fin(){
		spinner_image.setVisible(false);
	}
	
	protected void worker_updateparam(ArrayBuffer ab){
		ArrayBuffer ab2 = ab.slice(0);
		worker.transferMessage(SchedulingWorker.UPDATE_PARAM, ab);
		
		repeatingPost tmp = new repeatingPost(((Integer)SchedulingWorker.UPDATE_PARAM).toString(),
				ab2,
				DATA_TYPE.ARRAY_BUFFER,
				SchedulingWorker.UPDATE_PARAM);
		Scheduler.get().scheduleFixedDelay(tmp, DELAY_BETWEEN_CHILDREN);
	    spinner_image.setVisible(true);
	}
	
	protected void worker_updateparam_fin(){
		spinner_image.setVisible(false);
	}
	
	protected void worker_genauth(){
		spinner_image.setVisible(true);
		worker.postMessage(SchedulingWorker.GEN_AUTH,((Integer)(comboBox_numAuthAttr.getSelectedIndex()+1)).toString());
	}
	
	protected void worker_genpol(){
		worker.postMessage(SchedulingWorker.GEN_POL,((Integer)(comboBox_numAuthAttr.getSelectedIndex()+1)).toString());
	}
	
	private void worker_genpol_fin(String policy) {
		textBox_pol.setText(policy);
	}
	
	protected void worker_genpriv() {
		worker.postMessage(SchedulingWorker.GEN_PRIV, textBox_user_attr.getText());
	}

	private void worker_genauth_fin(String attr) {
		textBox_attr.setText(attr);
		String[] tmp = attr.split(" ");
		if (tmp.length!=0)
			comboBox_numAuthAttr.setSelectedIndex(tmp.length-1);
		spinner_image.setVisible(false);
	}
	
	private void processFiles_GP(File item) {
		final FileReader reader = new FileReader();
		reader.addLoadEndHandler(new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				if (reader.getError() == null) {
					worker_updateparam(reader.getArrayBufferResult());
				}
			}
		});
		reader.readAsArrayBuffer(item);
	}

	public void onModuleLoad() {
		startWorker();
		
		Streaming.registerStreamer();
	
		FlexTable panel = new FlexTable();
		panel.setCellPadding(3);
		panel.setCellSpacing(3);
		
		Label lblNewLabel = new Label("Load CurveParameter file:");
		lblNewLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	    lblNewLabel.setWidth("100%");
	    
	    fileUpload = new FileUploadExt();
	    fileUpload.setMultiple(false);
	    fileUpload.setWidth("100%");
	    fileUpload.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				processFiles_curveparams(fileUpload.getFiles().getItem(0));
			}});
	    
	    fileUpload_GP = new FileUploadExt();
	    fileUpload_GP.setMultiple(false);
	    fileUpload_GP.setWidth("100%");
	    fileUpload_GP.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				processFiles_GP(fileUpload_GP.getFiles().getItem(0));
			}});
	    
	    textArea_log = new TextArea();
	    textArea_log.setCharacterWidth(160);
	    textArea_log.setVisibleLines(100);
	    textArea_log.setSize("100%", "100%");
	    textArea_log.setReadOnly(true);
	    
	    Label lblOr = new Label("Thread num:");
	    lblOr.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	    lblOr.setStyleName("gwt-Label-OR");
	    panel.setWidget(0, 0, lblOr);
	    lblOr.setWidth("100%");
	    
	    comboBox_threadnum = new ListBox();
	    comboBox_threadnum.addChangeHandler(new ChangeHandler() {
	    	public void onChange(ChangeEvent event) {
	    		update_thread_num(comboBox_threadnum.getSelectedIndex()+1);
	    	}
	    });

	    for (int i=0; i<numCores_max; i++){
	    	comboBox_threadnum.addItem(((Integer)(i+1)).toString());
	    }
	    
	    comboBox_threadnum.setSelectedIndex(1);
	    panel.setWidget(0, 1, comboBox_threadnum);
	    comboBox_threadnum.setWidth("100%");
	    
	    panel.setWidget(0, 2, lblNewLabel);
	    panel.setWidget(0, 3, fileUpload);
	    
	    Label lblMaxThreadNum = new Label("Max Thread num:");
	    panel.setWidget(1, 0, lblMaxThreadNum);
	    
	    final ListBox comboBox = new ListBox();
	    comboBox.addItem("1");
	    comboBox.addItem("2");
	    comboBox.addItem("3");
	    comboBox.addItem("4");
	    comboBox.addItem("5");
	    comboBox.addItem("6");
	    comboBox.setSelectedIndex(5);
	    
	    comboBox.addChangeHandler(new ChangeHandler(){

			@Override
			public void onChange(ChangeEvent event) {
				numCores_max = comboBox.getSelectedIndex()+1;
				comboBox.setEnabled(false);
			}
	    	
	    });
	    
	    panel.setWidget(1, 1, comboBox);
	    comboBox.setWidth("100%");
	    panel.setWidget(1, 3, fileUpload_GP);
	    
	    Label lblLoadGlobalparameterFile = new Label("Load GlobalParameter file:");
	    lblLoadGlobalparameterFile.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	    panel.setWidget(1, 2, lblLoadGlobalparameterFile);
	    
	    Button btnExport = new Button("Export");
	    btnExport.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    		worker.postMessage(SchedulingWorker.DLD_GLOBAL, "");
	    	}
	    });
	    panel.setWidget(1, 4, btnExport);
	    btnExport.setWidth("100%");
	    
	    Label lblAttributes = new Label("Attributes: ");
	    lblAttributes.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	    panel.setWidget(2, 0, lblAttributes);
	    lblAttributes.setWidth("100%");
	    
	    textBox_attr = new TextBox();
	    panel.setWidget(2, 1, textBox_attr);
	    textBox_attr.setWidth("100%");
	    
	    Button btnCreateAuthority = new Button("Create Authority");
	    btnCreateAuthority.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    		worker_genauth();
	    	}
	    });
	    btnCreateAuthority.setText("Create Random Authority");
	    panel.setWidget(2, 2, btnCreateAuthority);
	    btnCreateAuthority.setWidth("100%");
	    
	    Button btnGenerate = new Button("Generate");
	    btnGenerate.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    		worker_genpol();
	    	}
	    });
	    
	    fileUpload_ak = new FileUploadExt();
	    fileUpload_ak.setMultiple(false);
		fileUpload_ak.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				processFiles_Ak(fileUpload_ak.getFiles().getItem(0));
			}});
		
	    panel.setWidget(2, 3, fileUpload_ak);
	    fileUpload_ak.setWidth("100%");
	    
	    Button btnNewButton_1 = new Button("Export");
	    btnNewButton_1.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    		//dld_file(ak_serialized);
	    		worker.postMessage(SchedulingWorker.DLD_AUTHORITY, "");
	    	}
	    });
	    panel.setWidget(2, 4, btnNewButton_1);
	    btnNewButton_1.setWidth("100%");
	    panel.setWidget(3, 2, btnGenerate);
	    btnGenerate.setWidth("100%");
	    
	    Label lblAccessPolicy = new Label("Access Policy:");
	    lblAccessPolicy.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	    panel.setWidget(3, 0, lblAccessPolicy);
	    lblAccessPolicy.setWidth("100%");
	    
	    textBox_pol = new TextBox();
	    panel.setWidget(3, 1, textBox_pol);
	    textBox_pol.setWidth("100%");
	    
	    Label lblAttribute = new Label("Attribute:");
	    panel.setWidget(3, 3, lblAttribute);
	    lblAttribute.setWidth("100%");
	    
	    comboBox_numAuthAttr = new ListBox();
	    for (int i=0; i<20; i++){
	    	comboBox_numAuthAttr.addItem(new Integer(i+1).toString());
	    }
	    panel.setWidget(3, 4, comboBox_numAuthAttr);
	    comboBox_numAuthAttr.setWidth("100%");
	    
	    Label lblUserAttributes = new Label("User Attributes:");
	    lblUserAttributes.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	    panel.setWidget(4, 0, lblUserAttributes);
	    lblUserAttributes.setWidth("100%");
	    
	    textBox_user_attr = new TextBox();
	    panel.setWidget(4, 1, textBox_user_attr);
	    textBox_user_attr.setWidth("100%");
	    
	    Button btnCreatePrivateKeys = new Button("Create Private Keys");
	    btnCreatePrivateKeys.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    		worker_genpriv();
	    	}
	    });
	    panel.setWidget(4, 2, btnCreatePrivateKeys);
	    btnCreatePrivateKeys.setWidth("100%");
	    
	    fileUpload_priv = new FileUploadExt();
	    fileUpload_priv.setMultiple(false);
	    fileUpload_priv.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				processFiles_priv(fileUpload_priv.getFiles().getItem(0));
			}});
	    
	    panel.setWidget(4, 3, fileUpload_priv);
	    fileUpload_priv.setWidth("100%");
	    
	    Button btnNewButton_5 = new Button("Export");
	    btnNewButton_5.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    		worker.postMessage(SchedulingWorker.DLD_PRIVATE, "");
	    	}
	    });
	    panel.setWidget(4, 4, btnNewButton_5);
	    btnNewButton_5.setWidth("100%");
	    
	    spinner_image = new Image("img/spinner.gif");
	    spinner_image.setSize("23px", "24px");
	    spinner_image.setVisible(false);
	    	    
	    btnDecrypt = new Button("Test Decrypt");
	    btnDecrypt.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    	    worker.postMessage(SchedulingWorker.START_DEC, "");
	    	    spinner_image.setVisible(true);
	    	    btnDecrypt.setEnabled(false);
	    	}
	    });
	    panel.setWidget(5, 0, btnDecrypt);
	    btnDecrypt.setWidth("100%");
	    	    
	    btnEncrypt = new Button("Test Encrypt");
	    btnEncrypt.addMouseUpHandler(new MouseUpHandler() {
	    	public void onMouseUp(MouseUpEvent event) {
	    		btnEncrypt.setEnabled(false);
	    		comboBox_threadnum.setEnabled(false);
	    		worker.postMessage(SchedulingWorker.START_ENC, "");
	    		spinner_image.setVisible(true);
	    	}
	    });
	    panel.setWidget(5, 1, btnEncrypt);
	    btnEncrypt.setWidth("100%");
	    panel.setWidget(5, 2, spinner_image);
	    
	    Label lblUriCap = new Label("URI cap:");
	    panel.setWidget(5, 4, lblUriCap);
	    lblUriCap.setWidth("100%");
	    
	    comboBox_URI_size = new ListBox();
	    comboBox_URI_size.addItem("1M");
	    comboBox_URI_size.addItem("2M");
	    comboBox_URI_size.addItem("5M");
	    comboBox_URI_size.addItem("10M");
	    comboBox_URI_size.addItem("20M");
	    panel.setWidget(5, 5, comboBox_URI_size);
	    comboBox_URI_size.setWidth("100%");
	    panel.setWidget(6, 0, textArea_log);
	    panel.getCellFormatter().setHeight(6, 0, "500");
	    RootPanel.get().add(panel, 9, 9);
	    panel.setSize("1383px", "705px");
	    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
	    panel.getFlexCellFormatter().setColSpan(2, 1, 2);
	    panel.getFlexCellFormatter().setColSpan(3, 1, 2);
	    panel.getFlexCellFormatter().setColSpan(4, 1, 2);
	    panel.getFlexCellFormatter().setColSpan(6, 0, 7);
	    panel.getFlexCellFormatter().setColSpan(3, 2, 2);
	    panel.getFlexCellFormatter().setColSpan(2, 4, 2);
	    panel.getFlexCellFormatter().setColSpan(4, 4, 2);
	    panel.getFlexCellFormatter().setColSpan(5, 0, 2);
	    panel.getFlexCellFormatter().setColSpan(0, 3, 4);
	    panel.getFlexCellFormatter().setColSpan(1, 3, 2);
	    panel.getFlexCellFormatter().setColSpan(1, 4, 2);
	    panel.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
	    FlexTableHelper.fixRowSpan(panel);
	}

	protected void update_thread_num(int i) {
		numCores = i;
		worker.postMessage(SchedulingWorker.UPDATE_CORENUM, ((Integer)i).toString());
	}

	protected void startWorker() {
		SchedulingWorkerFactory factory = GWT.create(SchedulingWorkerFactory.class);
        worker = factory.createAndStart();
        worker.setMessageHandler(this);
        
        child_workers = new Worker[numCores_max];
    	
    	for (int i=0; i<numCores_max; i++){
    		ChildWorkerFactory factory2 = GWT.create(ChildWorkerFactory.class);
    		child_workers[i] = factory2.createAndStart();
    		child_workers[i].setMessageHandler(this);
    		child_workers[i].postMessage(SchedulingWorker.SET_THREAD_ID, ((Integer)i).toString());
    	}
	}

	@Override
	public void onMessage(MessageEvent event) {
		if (event.getKind()==0){
			switch (event.getPrefix()){
			case SchedulingWorker.PROPAGATE_PK:
				repeatingPost tmp = new repeatingPost(((Integer)SchedulingWorker.PROPAGATE_PK).toString(), 
						event.getData_array(), 
						DATA_TYPE.ARRAY_BUFFER, 
						SchedulingWorker.PROPAGATE_PK);
				Scheduler.get().scheduleFixedDelay(tmp, DELAY_BETWEEN_CHILDREN);
				break;
			case SchedulingWorker.DLD_AUTHORITY:
				byte[] data = fromArrayBuffer(event.getData_array());
				dld_file(data, "ak.data");
				break;
			case SchedulingWorker.DLD_PRIVATE:
				byte[] data1 = fromArrayBuffer(event.getData_array());
				dld_file(data1, "priv.data");
				break;
			case SchedulingWorker.DLD_GLOBAL:
				byte[] data2 = fromArrayBuffer(event.getData_array());
				dld_file(data2, "gp.data");
				break;
			case SchedulingWorker.SCHEDULE_ENC:
			case SchedulingWorker.SCHEDULE_DEC:
				child_workers[presch.poll()].transferMessage(event.getPrefix(), event.getData_array());	//relay data to child
				break;
			case SchedulingWorker.CHILD_ENC_FIN:
				worker.transferMessage(SchedulingWorker.CHILD_ENC_FIN, event.getData_array());	//relay data to scheduler
				break;
			default:
				log("Wrong message!");
			}
		}else{
			String message = event.getData();
			int message_prefix = event.getPrefix();
			switch (message_prefix){
			case SchedulingWorker.DEFAULT_MESSAGE:
				log(message);
				break;
			case SchedulingWorker.POPULATE_PARAM_DONE:
				worker_populate_fin();
				break;
			case SchedulingWorker.UPDATE_PARAM_DONE:
				worker_updateparam_fin();
				break;
			case SchedulingWorker.GEN_AUTH_FIN:
				worker_genauth_fin(message);
				break;
			case SchedulingWorker.GEN_POL_FIN:
				worker_genpol_fin(message);
				break;
			case SchedulingWorker.GEN_PRIV_FIN:
				String returned_attr = message;
				if (returned_attr!=null && returned_attr.length()!=0 && returned_attr!=""){
					textBox_user_attr.setText(returned_attr);
				}
				break;
			case SchedulingWorker.FIN_ENC:
				log("Enc Done");
				spinner_image.setVisible(false);
				btnEncrypt.setEnabled(true);
				comboBox_threadnum.setEnabled(true);
				break;
			case SchedulingWorker.FIN_DEC:
				log("Dec Done");
				spinner_image.setVisible(false);
				btnDecrypt.setEnabled(true);
				break;
			case SchedulingWorker.PRE_SCHEDULE:
				presch.offer(Integer.parseInt(message));
				break;
			
			default:
				log("Error! Illegal msg!:"+message_prefix);
			}
		}
	}
    
    private byte[] fromArrayBuffer(ArrayBuffer ab) {
		byte[] data = new byte[ab.getByteLength()];
		Int8ArrayNative ia = Int8ArrayNative.create((com.google.gwt.typedarrays.shared.ArrayBuffer) ab);
		for (int i=0; i<data.length; i++){
			data[i]=ia.get(i);
		}
		return data;
	}

	protected void dld_file(byte[] data, String filename) {
		if (data.length<URIsize[comboBox_URI_size.getSelectedIndex()]){
			dld_file_internal(Base64Util.encode(data), filename);
			/*StringBuilder sb = new StringBuilder();
			sb.append("<a download=\"");
			sb.append(filename);
			sb.append("\"");
			sb.append("href=\"data:application/octet-stream;charset=us-ascii;base64,");
			sb.append(Base64Util.encode(data));
			sb.append("\">Click to download</a>");
			dld_file_newwind(sb.toString());*/
		}else{
			textArea_log.setText(Base91Util.encode(data));
		}
	}

	private void log(String string) {
		textArea_log.setText(textArea_log.getText()+string+"\n");
	}

	protected final native void dld_file_internal(String text, String f) /*-{
	
		var dwlObj = $doc.createElement("a");
		dwlObj.setAttribute("download", f);
		dwlObj.setAttribute("href", "data:application/octet-stream;base64," + text);
		var evt = $doc.createEvent("MouseEvents");
		evt.initMouseEvent("click", false, false, $wnd, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
		$wnd.alert("Download starting...");
		dwlObj.dispatchEvent(evt);
	
	}-*/;
	
	protected final native void dld_file_newwind(String content) /*-{
		var w = $wnd.open("#", "#");
		var d = w.document.open();
		d.write(content);
		d.close();
	}-*/;
	
	class repeatingPost implements RepeatingCommand{
		
		public repeatingPost(String message, ArrayBuffer arrayBuffer, DATA_TYPE type, int prefix){
			this.message=message;
			message_array=arrayBuffer;
			this.type=type;
			this.prefix=prefix;
		}
		
		final public DATA_TYPE type;
		final public ArrayBuffer message_array;
		int i=0;
		final int prefix;
		final String message;
		@Override
		public boolean execute() {
			if (i<numCores_max){
				if (type==DATA_TYPE.STRING){
					child_workers[i].postMessage(prefix, message);
				}else{
					if (i==numCores_max-1){
						child_workers[i].transferMessage(prefix, message_array);
					}else{
						ArrayBuffer tmp = message_array.slice(0);
						child_workers[i].transferMessage(prefix, tmp);
					}
				}
				i++;
				if (i>=numCores_max) return false; else return true;
			}
			return false;
		}
		
	}
}
