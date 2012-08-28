package oripa.paint.deletevertex;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import oripa.paint.GraphicMouseAction;
import oripa.paint.MouseContext;

public class DeleteVertexAction extends GraphicMouseAction {


	public DeleteVertexAction(){
		setEditMode(EditMode.OTHER);

		setActionState(new DeletingVertex());

	}


	@Override
	public void onDragged(MouseContext context, AffineTransform affine,
			MouseEvent event) {

	}


	@Override
	public void onReleased(MouseContext context, AffineTransform affine, MouseEvent event) {


	}

	@Override
	public void onDraw(Graphics2D g2d, MouseContext context) {

		super.onDraw(g2d, context);

		drawPickCandidateVertex(g2d, context);
	}

	@Override
	public void onPressed(MouseContext context, AffineTransform affine,
			MouseEvent event) {
	}



}
