package com.pip.image.workshop.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.WorkshopPlugin;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.swtdesigner.ResourceManager;

public class ImageCompareEditor extends EditorPart {
	public static final String ID = "com.pip.image.workshop.editor.ImageCompareEditor"; //$NON-NLS-1$
	
	protected File rootDir1;
	protected File rootDir2;
	protected FolderCompareResult compareResult;
	protected boolean onlyShowDiff = false;
	protected boolean onlyShowBoth = false;
	protected boolean onlyShowFrameDiff = false;
	
	protected FileCompareResult currentFile;
	protected PipImage leftImage;
	protected PipImage rightImage;
	
	protected static class FolderCompareResult {
		public FolderCompareResult parent;
		public String path;
		public int result;		// 0 - 只有左边有、1 - 只有右边有、2 - 相同、3 - 不同
		public List<FileCompareResult> files = new ArrayList<FileCompareResult>();
		public List<FolderCompareResult> folders = new ArrayList<FolderCompareResult>();
		
		public String getName() {
			if (path.length() == 0) {
				return "<root>";
			} else {
				int pos = path.lastIndexOf('/');
				if (pos == -1) {
					return path;
				} else {
					return path.substring(pos + 1);
				}
			}
		}
	}
	
	protected static class FileCompareResult {
		public FolderCompareResult parent;
		public String path;
		public int result; 		// 0 - 只有左边有、1 - 只有右边有、2 - 相同、3 - 帧数相同但内容不同、4 - 帧数不同、5 - 比较出错
		
		public String getName() {
			if (path.length() == 0) {
				return "<root>";
			} else {
				int pos = path.lastIndexOf('/');
				if (pos == -1) {
					return path;
				} else {
					return path.substring(pos + 1);
				}
			}
		}
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof FolderCompareResult) {
				FolderCompareResult fr = (FolderCompareResult)element;
				if (columnIndex == 0) {
					if (fr.result != 1) {
						return fr.getName();
					} else {
						return "";
					}
				} else if (columnIndex == 1) {
					if (fr.result != 0) {
						return fr.getName();
					} else {
						return "";
					}
				} else {
					switch (fr.result) {
					case 0:
						return "仅左侧";
					case 1:
						return "仅右侧";
					case 2:
						return "相同";
					case 3:
						return "不同";
					}
				}
			} else if (element instanceof FileCompareResult) {
				FileCompareResult fr = (FileCompareResult)element;
				if (columnIndex == 0) {
					if (fr.result != 1) {
						return fr.getName();
					} else {
						return "";
					}
				} else if (columnIndex == 1) {
					if (fr.result != 0) {
						return fr.getName();
					} else {
						return "";
					}
				} else {
					switch (fr.result) {
					case 0:
						return "仅左侧";
					case 1:
						return "仅右侧";
					case 2:
						return "相同";
					case 3:
						return "内容不同";
					case 4:
						return "帧数不同";
					case 5:
						return "出错";
					}
				}
			}
			return element.toString();
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 2) {
				return null;
			}
			if (element instanceof FolderCompareResult) {
				FolderCompareResult fr = (FolderCompareResult)element;
				if (fr.result == 2) {
					return ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/equal.gif");
				} else {
					return ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/notequal.gif");
				}
			} else if (element instanceof FileCompareResult) {
				FileCompareResult fr = (FileCompareResult)element;
				if (fr.result == 2) {
					return ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/equal.gif");
				} else {
					return ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/notequal.gif");
				}
			}
			return null;
		}
	}
	
	class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof FolderCompareResult) {
				FolderCompareResult fr = (FolderCompareResult)parentElement;
				List<Object> ret = new ArrayList<Object>();
				for (int i = 0; i < fr.folders.size(); i++) {
					if (onlyShowDiff && fr.folders.get(i).result == 2) {
						continue;
					}
					if (onlyShowBoth && fr.folders.get(i).result < 2) {
						continue;
					}
					ret.add(fr.folders.get(i));
				}
				for (int i = 0; i < fr.files.size(); i++) {
					if (onlyShowDiff && fr.files.get(i).result == 2) {
						continue;
					}
					if (onlyShowBoth && fr.files.get(i).result < 2) {
						continue;
					}
					if (onlyShowFrameDiff && fr.files.get(i).result != 4) {
						continue;
					}
					ret.add(fr.files.get(i));
				}
				return ret.toArray();
			} else {
				return new Object[0];
			}
		}
		public Object getParent(Object element) {
			if (element instanceof FolderCompareResult) {
				FolderCompareResult fr = (FolderCompareResult)element;
				return fr.parent;
			} else if (element instanceof FileCompareResult) {
				FileCompareResult fr = (FileCompareResult)element;
				return fr.parent;
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}
	
	private Table fileListTree;
	private TableTree fileList;
	private Text textFolder2;
	private Text textFolder1;

	private TableTreeViewer fileListViewer;
	private ImageViewer previewer1;
	private ImageViewer previewer2;
	
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		container.setLayout(gridLayout);

		final Label label = new Label(container, SWT.NONE);
		label.setText("目录1：");

		textFolder1 = new Text(container, SWT.BORDER);
		final GridData gd_textFolder1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textFolder1.setLayoutData(gd_textFolder1);

		final Button buttonBrowse1 = new Button(container, SWT.NONE);
		buttonBrowse1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(getSite().getShell(), SWT.OPEN);
				if (textFolder1.getText().trim().length() > 0) {
					dlg.setFilterPath(textFolder1.getText().trim());
				}
				String dir = dlg.open();
				if (dir != null) {
					textFolder1.setText(dir);
				}
			}
		});
		buttonBrowse1.setText("浏览...");

		final Button buttonCompare = new Button(container, SWT.NONE);
		buttonCompare.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				File dir1 = new File(textFolder1.getText().trim());
				File dir2 = new File(textFolder2.getText().trim());
				if (!dir1.exists() || !dir1.isDirectory() || !dir2.exists() || !dir2.isDirectory()) {
					MessageDialog.openError(getSite().getShell(), "错误", "请选择两个目录再开始比较。");
					return;
				}
				FolderCompareResult fr = compareFolder(dir1, dir2, "");
				rootDir1 = dir1;
				rootDir2 = dir2;
				compareResult = fr;
				fileListViewer.setInput(compareResult);
			}
		});
		final GridData gd_buttonCompare = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 2);
		buttonCompare.setLayoutData(gd_buttonCompare);
		buttonCompare.setText("开始比较");

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("目录2：");

		textFolder2 = new Text(container, SWT.BORDER);
		final GridData gd_textFolder2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textFolder2.setLayoutData(gd_textFolder2);

		final Button buttonBrowse2 = new Button(container, SWT.NONE);
		buttonBrowse2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(getSite().getShell(), SWT.OPEN);
				if (textFolder2.getText().trim().length() > 0) {
					dlg.setFilterPath(textFolder2.getText().trim());
				}
				String dir = dlg.open();
				if (dir != null) {
					textFolder2.setText(dir);
				}
			}
		});
		buttonBrowse2.setText("浏览...");

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		final SashForm sashForm = new SashForm(composite, SWT.NONE);

		fileListViewer = new TableTreeViewer(sashForm, SWT.FULL_SELECTION | SWT.BORDER);
		fileListViewer.setLabelProvider(new TableLabelProvider());
		fileListViewer.setContentProvider(new TreeContentProvider());
		fileList = fileListViewer.getTableTree();
		fileListTree = fileList.getTable();
		fileListTree.setHeaderVisible(true);
		fileListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent evt) {
				StructuredSelection sel = (StructuredSelection)evt.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				Object obj = sel.getFirstElement();
				if (obj instanceof FileCompareResult) {
					FileCompareResult fr = (FileCompareResult)obj;
					
					File file1 = new File(rootDir1, fr.path);
					PipImage image1 = new PipImage();
					if (file1.exists()) {
						try {
							image1.load(file1.getAbsolutePath());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					previewer1.setFlatMode(true);
					previewer1.setInput(image1);
					previewer1.refresh();
					
					File file2 = new File(rootDir2, fr.path);
					PipImage image2 = new PipImage();
					if (file2.exists()) {
						try {
							image2.load(file2.getAbsolutePath());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					previewer2.setFlatMode(true);
					previewer2.setInput(image2);
					previewer2.refresh();
					
					currentFile = fr;
					leftImage = image1;
					rightImage = image2;
				}
			}
		});

		final TableColumn file1TableColumn = new TableColumn(fileListTree, SWT.NONE);
		file1TableColumn.setWidth(100);
		file1TableColumn.setText("File1");

		final TableColumn file2TableColumn = new TableColumn(fileListTree, SWT.NONE);
		file2TableColumn.setWidth(100);
		file2TableColumn.setText("File2");

		final TableColumn statusTableColumn = new TableColumn(fileListTree, SWT.NONE);
		statusTableColumn.setWidth(100);
		statusTableColumn.setText("Status");

		final SashForm sashForm_1 = new SashForm(sashForm, SWT.VERTICAL);

		final Composite file1PreviewContainer = new Composite(sashForm_1, SWT.NONE);
		file1PreviewContainer.setLayout(new FillLayout());
		
		previewer1 = new ImageViewer(file1PreviewContainer, SWT.NONE);

		final Composite file2PreviewContainer = new Composite(sashForm_1, SWT.NONE);
		file2PreviewContainer.setLayout(new FillLayout());
		
		previewer2 = new ImageViewer(file2PreviewContainer, SWT.NONE);
		
		sashForm_1.setWeights(new int[] {1, 1 });

		final Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 5;
		composite_1.setLayout(gridLayout_1);

		final Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 3;
		composite_2.setLayout(gridLayout_2);

		final Button buttonShowDiffOnly = new Button(composite_2, SWT.CHECK);
		buttonShowDiffOnly.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onlyShowDiff = buttonShowDiffOnly.getSelection();
				fileListViewer.refresh();
			}
		});
		buttonShowDiffOnly.setText("只显示不同文件");

		final Button buttonShowBothOnly = new Button(composite_2, SWT.CHECK);
		buttonShowBothOnly.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onlyShowBoth = buttonShowBothOnly.getSelection();
				fileListViewer.refresh();
			}
		});
		buttonShowBothOnly.setText("只显示两边都有的文件");

		final Button buttonShowFrameDiffOnly = new Button(composite_2, SWT.CHECK);
		buttonShowFrameDiffOnly.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onlyShowFrameDiff = buttonShowFrameDiffOnly.getSelection();
				fileListViewer.refresh();
			}
		});
		buttonShowFrameDiffOnly.setText("只显示帧数不同的文件");

		final Button buttonCopyDownAll = new Button(composite_1, SWT.NONE);
		buttonCopyDownAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					onCopyDownAll();
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		buttonCopyDownAll.setText("    ∨∨    ");

		final Button buttonCopyDown = new Button(composite_1, SWT.NONE);
		buttonCopyDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					onCopyDown();
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		buttonCopyDown.setText("    ∨    ");

		final Button buttonCopyUp = new Button(composite_1, SWT.NONE);
		buttonCopyUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					onCopyUp();
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		buttonCopyUp.setText("    ∧    ");

		final Button buttonCopyUpAll = new Button(composite_1, SWT.NONE);
		buttonCopyUpAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					onCopyUpAll();
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		final GridData gd_buttonCopyUpAll = new GridData();
		buttonCopyUpAll.setLayoutData(gd_buttonCopyUpAll);
		buttonCopyUpAll.setText("    ∧∧    ");
		sashForm.setWeights(new int[] {1, 3 });

		this.setPartName("图片比较工具");
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
		// Do the Save As operation
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		// Initialize the editor part
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	/**
	 * 比较一个目录及其子目录中所有的pip文件。
	 * @param root1
	 * @param root2
	 * @param path
	 * @return
	 */
	protected FolderCompareResult compareFolder(File root1, File root2, String path) {
		File dir1, dir2;
		if (path.length() == 0) {
			dir1 = root1;
			dir2 = root2;
		} else {
			dir1 = new File(root1, path);
			dir2 = new File(root2, path);
		}
		FolderCompareResult ret = new FolderCompareResult();
		ret.path = path;
		File[] fileList1 = dir1.listFiles();
		File[] fileList2 = dir2.listFiles();
		Arrays.sort(fileList1);
		Arrays.sort(fileList2);
		
		// 首先比较两边的子目录
		String addPath = path.length() == 0 ? "" : path + "/";
		for (int i = 0, j = 0; i < fileList1.length || j < fileList2.length; ) {
			if (i >= fileList1.length) {
				// 左边已无
				if (fileList2[j].isDirectory()) {
					// 生成右边文件列表
					FolderCompareResult rr = generateSingleSideResult(fileList2[j], addPath + fileList2[j].getName(), 1);
					rr.parent = ret;
					ret.folders.add(rr);
				}
				j++;
			} else if (j >= fileList2.length) {
				// 右边已无
				if (fileList1[i].isDirectory()) {
					// 生成左边文件列表
					FolderCompareResult rr = generateSingleSideResult(fileList1[i], addPath + fileList1[i].getName(), 0);
					rr.parent = ret;
					ret.folders.add(rr);
				}
				i++;
			} else if (!fileList1[i].isDirectory()) {
				i++;
			} else if (!fileList2[j].isDirectory()) {
				j++;
			} else {
				int r = fileList1[i].getName().compareTo(fileList2[j].getName());
				if (r < 0) {
					// 生成左边文件列表
					FolderCompareResult rr = generateSingleSideResult(fileList1[i], addPath + fileList1[i].getName(), 0);
					rr.parent = ret;
					ret.folders.add(rr);
					i++;
				} else if (r > 0) {
					// 生成右边文件列表
					FolderCompareResult rr = generateSingleSideResult(fileList2[j], addPath + fileList2[j].getName(), 1);
					rr.parent = ret;
					ret.folders.add(rr);
					j++;
				} else {
					// 比较两个目录
					FolderCompareResult rr = compareFolder(root1, root2, addPath + fileList1[i].getName());
					rr.parent = ret;
					ret.folders.add(rr);
					i++;
					j++;
				}
			}
		}
		
		// 比较两边的文件
		for (int i = 0, j = 0; i < fileList1.length || j < fileList2.length; ) {
			if (i >= fileList1.length) {
				// 左边已无
				if (fileList2[j].isFile() && fileList2[j].getName().toLowerCase().endsWith(".pip")) {
					// 右边文件单边
					FileCompareResult rr = new FileCompareResult();
					rr.parent = ret;
					rr.path = addPath + fileList2[j].getName();
					rr.result = 1;
					ret.files.add(rr);
				}
				j++;
			} else if (j >= fileList2.length) {
				// 右边已无
				if (fileList1[i].isFile() && fileList1[i].getName().toLowerCase().endsWith(".pip")) {
					// 左边文件单边
					FileCompareResult rr = new FileCompareResult();
					rr.parent = ret;
					rr.path = addPath + fileList1[i].getName();
					rr.result = 0;
					ret.files.add(rr);
				}
				i++;
			} else if (!fileList1[i].isFile() || !fileList1[i].getName().toLowerCase().endsWith(".pip")) {
				i++;
			} else if (!fileList2[j].isFile() || !fileList2[j].getName().toLowerCase().endsWith(".pip")) {
				j++;
			} else {
				int r = fileList1[i].getName().toLowerCase().compareTo(fileList2[j].getName().toLowerCase());
				if (r < 0) {
					// 左边文件单边
					FileCompareResult rr = new FileCompareResult();
					rr.parent = ret;
					rr.path = addPath + fileList1[i].getName();
					rr.result = 0;
					ret.files.add(rr);
					i++;
				} else if (r > 0) {
					// 右边文件单边
					FileCompareResult rr = new FileCompareResult();
					rr.parent = ret;
					rr.path = addPath + fileList2[j].getName();
					rr.result = 1;
					ret.files.add(rr);
					j++;
				} else {
					// 比较两个文件
					FileCompareResult rr = new FileCompareResult();
					rr.parent = ret;
					rr.path = addPath + fileList1[i].getName();
					try {
						rr.result = compareFile(fileList1[i], fileList2[j]);
					} catch (Exception e) {
						rr.result = 5;
					}
					ret.files.add(rr);
					i++;
					j++;
				}
			}
		}
		
		ret.result = 2;
		for (FolderCompareResult fr : ret.folders) {
			if (fr.result != 2) {
				ret.result = 3;
				break;
			}
		}
		for (FileCompareResult fr : ret.files) {
			if (fr.result != 2) {
				ret.result = 3;
				break;
			}
		}
		
		return ret;
	}
	
	// 比较2个pip文件
	// 返回：2 - 相同、3 - 帧数相同但内容不同、4 - 帧数不同 
	protected int compareFile(File file1, File file2) throws IOException {
		byte[] data1 = Utils.loadFileData(file1);
		byte[] data2 = Utils.loadFileData(file2);
		if (data1.length == data2.length && Arrays.equals(data1, data2)) {
			return 2;
		}
		
		PipImage img1 = new PipImage();
		img1.load(new ByteArrayInputStream(data1));
		PipImage img2 = new PipImage();
		img2.load(new ByteArrayInputStream(data2));
		if (img1.getImgCount() != img2.getImgCount()) {
			return 4;
		} else {
			return 3;
		}
	}
	
	// 单边记录
	protected FolderCompareResult generateSingleSideResult(File dir, String path, int result) {
		FolderCompareResult ret = new FolderCompareResult();
		ret.path = path;
		ret.result = result;
		File[] fileList = dir.listFiles();
		Arrays.sort(fileList);

		// 先生成子目录记录
		String addPath = path.length() == 0 ? "" : path + "/";
		for (File f : fileList) {
			if (f.isDirectory()) {
				FolderCompareResult rr = generateSingleSideResult(f, addPath + f.getName(), result);
				rr.parent = ret;
				ret.folders.add(rr);
			}
		}
		
		// 生成文件记录
		for (File f : fileList) {
			if (f.isFile() && f.getName().toLowerCase().endsWith(".pip")) {
				FileCompareResult rr = new FileCompareResult();
				rr.parent = ret;
				rr.path = addPath + f.getName();
				rr.result = result;
				ret.files.add(rr);
			}
		}
		return ret;
	}
	
	/**
	 * 把左侧文件拷贝到右侧。
	 */
	protected void onCopyDownAll() throws IOException {
		if (currentFile == null) {
			return;
		}
		File leftFile = new File(rootDir1, currentFile.path);
		if (!leftFile.exists()) {
			return;
		}
		File rightFile = new File(rootDir2, currentFile.path);
		if (!rightFile.exists()) {
			rightFile.getParentFile().mkdirs();
		}
		Utils.copyFile(leftFile, rightFile);

		rightImage = new PipImage();
		rightImage.load(rightFile.getAbsolutePath());
		previewer2.setInput(rightImage);
		previewer2.refresh();

		currentFile.result = 2;
		fileListViewer.refresh(currentFile);
	}
	
	/**
	 * 把左侧文件选中的帧复制到右侧。
	 */
	protected void onCopyDown() throws Exception {
		if (currentFile == null) {
			return;
		}
		int[] sels = previewer1.getSelectedFrames();
		if (sels.length == 0) {
			MessageDialog.openInformation(getSite().getShell(), "错误", "请先在上方图片中选择要拷贝的帧。");
			return;
		}
		File rightFile = new File(rootDir2, currentFile.path);
		if (!rightFile.exists()) {
			rightFile.getParentFile().mkdirs();
		}
		Arrays.sort(sels);
		for (int i = 0; i < sels.length; i++) {
			int frame = sels[i];
			Image img = leftImage.getImageDraw(frame).createSWTImage(getSite().getShell().getDisplay(), 0);
			int[][] imgData = SWTUtils.getImageData(img, new com.pip.util.Rectangle(0, 0, img.getBounds().width, img.getBounds().height));
			img.dispose();
			if (frame >= rightImage.getImgCount()) {
				// add new frame
				rightImage.addFrame(imgData);
			} else {
				// update existing frame
				rightImage.addFrame(imgData);
				PipImageData newid = rightImage.getImageDatas().remove(rightImage.getImgCount() - 1);
				rightImage.getImageDatas().remove(frame);
				rightImage.getImageDatas().add(frame, newid);
			}
		}
		rightImage.save(rightFile);
		previewer2.setInput(rightImage);
		previewer2.refresh();
	}
	
	/**
	 * 把右侧文件复制到左侧。
	 */
	protected void onCopyUpAll() throws IOException {
		if (currentFile == null) {
			return;
		}
		File rightFile = new File(rootDir2, currentFile.path);
		if (!rightFile.exists()) {
			return;
		}
		File leftFile = new File(rootDir1, currentFile.path);
		if (!leftFile.exists()) {
			leftFile.getParentFile().mkdirs();
		}
		Utils.copyFile(rightFile, leftFile);

		leftImage = new PipImage();
		leftImage.load(rightFile.getAbsolutePath());
		previewer1.setInput(leftImage);
		previewer1.refresh();

		currentFile.result = 2;
		fileListViewer.refresh(currentFile);
	}

	/**
	 * 把右侧文件选中的帧复制到左侧。
	 */
	protected void onCopyUp() throws IOException {
		if (currentFile == null) {
			return;
		}
		int[] sels = previewer2.getSelectedFrames();
		if (sels.length == 0) {
			MessageDialog.openInformation(getSite().getShell(), "错误", "请先在下方图片中选择要拷贝的帧。");
			return;
		}
		File leftFile = new File(rootDir1, currentFile.path);
		if (!leftFile.exists()) {
			leftFile.getParentFile().mkdirs();
		}
		Arrays.sort(sels);
		for (int i = 0; i < sels.length; i++) {
			int frame = sels[i];
			Image img = rightImage.getImageDraw(frame).createSWTImage(getSite().getShell().getDisplay(), 0);
			int[][] imgData = SWTUtils.getImageData(img, new com.pip.util.Rectangle(0, 0, img.getBounds().width, img.getBounds().height));
			img.dispose();
			if (frame >= leftImage.getImgCount()) {
				// add new frame
				leftImage.addFrame(imgData);
			} else {
				// update existing frame
				leftImage.addFrame(imgData);
				PipImageData newid = leftImage.getImageDatas().remove(leftImage.getImgCount() - 1);
				leftImage.getImageDatas().remove(frame);
				leftImage.getImageDatas().add(frame, newid);
			}
		}
		leftImage.save(leftFile);
		previewer1.setInput(leftImage);
		previewer1.refresh();
	}
}
