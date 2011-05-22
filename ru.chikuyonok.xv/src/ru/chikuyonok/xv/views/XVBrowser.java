package ru.chikuyonok.xv.views;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.BrowserViewer;

import ru.chikuyonok.xv.XVBrowserPlugin;


/**
 * A Web browser widget. It extends the Eclipse SWT Browser widget by adding an
 * optional toolbar complete with a URL combo box, history, back & forward, and
 * refresh buttons.
 * <p>
 * Use the style bits to choose which toolbars are available within the browser
 * composite. You can access the embedded SWT Browser directly using the
 * getBrowser() method.
 * </p>
 * <p>
 * Additional capabilities are available when used as the internal Web browser,
 * including status text and progress on the Eclipse window's status line, or
 * moving the toolbar capabilities up into the main toolbar.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LOCATION_BAR, BUTTON_BAR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>None</dd>
 * </dl>
 * 
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class XVBrowser extends BrowserViewer {
	public static final String PROPERTY_TITLE = "title"; //$NON-NLS-1$
	
	protected String xvTemplate;
	private ProgressListener progressListener;
	private String lastUrl = "";
	
	private final BrowserFunction function;
	
	private static class ClipboardFunction extends BrowserFunction {
		private final Clipboard cb;
		ClipboardFunction (Browser browser, String name) {
			super (browser, name);
			cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
		}
		
		public Object function (Object[] arguments) {
			if (arguments != null && arguments.length > 0) {
				TextTransfer textTransfer = TextTransfer.getInstance();
		        cb.setContents(new Object[] { arguments[0] },
		            new Transfer[] { textTransfer });
			}
			
			return null;
		}
	}

	public XVBrowser(Composite parent, int style) {
		super(parent, style);
		
		function = new ClipboardFunction(browser, "copyToClipboard");
		
		progressListener = new ProgressListener() {
			@Override
			public void completed(ProgressEvent event) {
				browser.removeProgressListener(progressListener);
				
				String xmlSource = forXML(browser.getText());
				if (!xmlSource.equals("")) {
					if (xvTemplate == null) {
						xvTemplate = XVBrowserPlugin.getXVTemplate();
					}
					
					browser.setText(xvTemplate.replace("@SOURCE@", xmlSource), true);
					browser.setRedraw(true);
				}
			}
			
			@Override
			public void changed(ProgressEvent event) {}
		};
		
		setupDND();
	}
	
	/**
     * Loads a URL.
     * 
     * @param url
     *            the URL to be loaded
     * @return true if the operation was successful and false otherwise.
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the url is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #getURL()
     */
    public void setURL(String url) {
       setURL(url, true);
    }
    
    private void setURL(String url, boolean browse) {
        if (url == null) {
            home();
            return;
        }

        if (browse)
            navigate(url);

        addToHistory(url);
        updateHistory();
    }
    
    public void refresh() {
        if (browser != null) // navigate to URL instead refresh
            navigate(lastUrl);
        else
            text.refresh();
		  try {
			  Thread.sleep(50);
		  } catch (Exception e) {
			  // ignore
		  }
    }
    
	private boolean navigate(String url) {
        if (browser != null) {
        	lastUrl = url;
        	browser.setRedraw(false);
        	browser.addProgressListener(progressListener);
        	return browser.setUrl(url, null, new String[] {"Cache-Control: no-cache"}); //$NON-NLS-1$
        }
        	
        return text.setUrl(url);
    }
	
	public static String forXML(String aText) {
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(
				aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '\'') {
				result.append("&#039;");
			} else if (character == '&') {
				result.append("&amp;");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}
	
	private void setupDND() {
		DragSource source = new DragSource(browser, DND.DROP_MOVE | DND.DROP_COPY);
		 
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		 
		source.addDragListener(new DragSourceListener() {
		   public void dragStart(DragSourceEvent event) {}
		   public void dragSetData(DragSourceEvent event) {}
		   public void dragFinished(DragSourceEvent event) {}
		});
	}
	
	public void dispose() {
		super.dispose();
		function.dispose();
	}
	
	public boolean setFocus() {
        if (browser != null) {
            browser.setFocus();
            updateHistory();
            return true;
        }
        return super.setFocus();
    }
}
