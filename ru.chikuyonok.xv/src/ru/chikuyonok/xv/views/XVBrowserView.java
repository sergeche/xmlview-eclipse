package ru.chikuyonok.xv.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class XVBrowserView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ru.chikuyonok.xv.views.XVBrowserView";
	protected XVBrowser browser;

	/**
	 * The constructor.
	 */
	public XVBrowserView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@SuppressWarnings("restriction")
	public void createPartControl(Composite parent) {
		browser = new XVBrowser(parent, XVBrowser.BUTTON_BAR | XVBrowser.LOCATION_BAR);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		browser.setFocus();
	}
}