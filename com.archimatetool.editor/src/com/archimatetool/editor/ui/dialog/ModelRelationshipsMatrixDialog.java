/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.editor.ui.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.ui.ArchiLabelProvider;
import com.archimatetool.editor.ui.IArchiImages;
import com.archimatetool.editor.ui.components.ExtendedTitleAreaDialog;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.util.RelationshipsMatrix;



/**
 * Model Relationships matrix Dialog
 * 
 * @author Phillip Beauvoir
 */
public class ModelRelationshipsMatrixDialog extends ExtendedTitleAreaDialog {
    
    private static String HELP_ID = "com.archimatetool.help.ModelRelationshipsMatrixDialog"; //$NON-NLS-1$
    
    private IArchimateModel model;
    private List<IArchimateConcept> concepts;
    
    public ModelRelationshipsMatrixDialog(Shell parentShell, IArchimateModel model) {
        super(parentShell, "ModelRelationshipsMatrixDialog"); //$NON-NLS-1$
        setTitleImage(IArchiImages.ImageFactory.getImage(IArchiImages.ECLIPSE_IMAGE_NEW_WIZARD));
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.model = model;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.RelationshipsMatrixDialog_0);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELP_ID);

        setTitle(Messages.RelationshipsMatrixDialog_0);
        setMessage("Model Relationships");
        Composite composite = (Composite)super.createDialogArea(parent);

        Composite client = new Composite(composite, SWT.NULL);
        GridLayout layout = new GridLayout(2, false);
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        GridData gd;
        
        GridTableViewer viewer = new GridTableViewer(client);
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 800;
        gd.heightHint = 500;
        viewer.getControl().setLayoutData(gd);
        
        viewer.getGrid().setHeaderVisible(true);
        viewer.getGrid().setRowHeaderVisible(true);
        viewer.getGrid().setRowsResizeable(true);
        viewer.getGrid().setCellSelectionEnabled(true);
        
        viewer.setRowHeaderLabelProvider(new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                cell.setText(((IArchimateConcept)cell.getElement()).getName());
                cell.setImage(ArchiLabelProvider.INSTANCE.getImage(cell.getElement()));
            }
        });
        
        for(IArchimateConcept concept : getData()) {
            GridColumn column = new GridColumn(viewer.getGrid(), SWT.NONE);
            column.setWidth(96);
            column.setImage(ArchiLabelProvider.INSTANCE.getImage(concept));
            column.setHeaderTooltip(concept.getName());
            column.setText(concept.getName());
        }
        
        viewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
            
            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return getData().toArray();
            }
        });
        
        viewer.setLabelProvider(new MyLabelProvider());
        
        viewer.setInput(getData());
        
        String text = ""; //$NON-NLS-1$
        for(Entry<EClass, Character> entry : RelationshipsMatrix.INSTANCE.getRelationshipsValueMap().entrySet()) {
            text += entry.getValue() + ": " + ArchiLabelProvider.INSTANCE.getDefaultName(entry.getKey()) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        Label label = new Label(client, SWT.NULL);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.TOP, SWT.TOP, false, true));
        
        return composite;
    }
    
    private List<IArchimateConcept> getData() {
        if(concepts == null) {
            concepts = new ArrayList<>();
            
            for(Iterator<EObject> iter = model.eAllContents(); iter.hasNext();) {
                EObject eObject = iter.next();
                if(eObject instanceof IArchimateElement) {
                    concepts.add((IArchimateConcept)eObject);
                }
            }
        }
        
        return concepts;
    }
    
    private class MyLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            IArchimateConcept conceptRow = (IArchimateConcept)element;
            IArchimateConcept conceptColumn = getData().get(columnIndex);
            
            String text = ""; //$NON-NLS-1$
            
            for(IArchimateRelationship r : conceptRow.getSourceRelationships()) {
                if(r.getTarget() == conceptColumn) {
                    text += getRelationshipLetter(r) + ", "; //$NON-NLS-1$
                }
            }
            
            return text.replaceAll(", $", ""); // Remove final comma  //$NON-NLS-1$//$NON-NLS-2$
        }
    }
    
    private String getRelationshipLetter(IArchimateRelationship r) {
        return RelationshipsMatrix.INSTANCE.getRelationshipsValueMap().get(r.eClass()).toString();
    }
    
    @Override
    protected Point getDefaultDialogSize() {
        return new Point(1000, 700);
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK button
        createButton(parent, IDialogConstants.OK_ID, Messages.RelationshipsMatrixDialog_2, true);
    }
}
