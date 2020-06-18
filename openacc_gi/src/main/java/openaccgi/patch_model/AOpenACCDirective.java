package openaccgi.patch_model;

import java.io.File;

/*package*/ abstract class AOpenACCDirective extends ADirective {

	protected AOpenACCDirective(File f, boolean isScopeCreator) {
		super(f, isScopeCreator);
	}
}
