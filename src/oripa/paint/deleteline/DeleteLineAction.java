package oripa.paint.deleteline;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;

import oripa.ORIPA;
import oripa.geom.OriLine;
import oripa.geom.RectangleClipper;
import oripa.paint.GraphicMouseAction;
import oripa.paint.MouseContext;
import oripa.paint.RectangularSelectableAction;

public class DeleteLineAction extends RectangularSelectableAction {


	public DeleteLineAction(){
		setEditMode(EditMode.OTHER);

		setActionState(new DeletingLine());

	}

	@Override
	public void onDraw(Graphics2D g2d, MouseContext context) {

		super.onDraw(g2d, context);

		drawPickCandidateLine(g2d, context);
	}

	@Override
	protected void afterRectangularSelection(Collection<OriLine> selectedLines,
			MouseContext context) {

		if(selectedLines.isEmpty() == false){
			ORIPA.doc.pushUndoInfo();
			for (OriLine l : selectedLines) {
				ORIPA.doc.removeLine(l);
			}
		}		
	}



}
