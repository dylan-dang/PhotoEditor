public interface Command {
	void execute();
	void undo();
	void redo();
}

public class setPixel implements Command {

	public setPixel() {

	}

	@Override
	public void execute() {
	}

	@Override
	public void undo() {
	}

	@Override
	public void redo() {
	}
}
