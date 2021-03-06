package oripa.domain.paint.deletevertex;

import oripa.domain.cptool.Painter;
import oripa.domain.paint.PaintContextInterface;
import oripa.domain.paint.core.PickingVertex;

public class DeletingVertex extends PickingVertex {

	@Override
	protected void initialize() {

	}

	@Override
	protected void onResult(final PaintContextInterface context, final boolean doSpecial) {

		if (context.getVertexCount() > 0) {
			context.creasePatternUndo().pushUndoInfo();

			Painter painter = context.getPainter();
			painter.removeVertex(context.popVertex());

		}

		context.clear(false);
	}

}
