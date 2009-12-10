package org.openscada.ae.server.storage.memory.internal;

import java.beans.PropertyEditor;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.openscada.core.Variant;
import org.openscada.core.VariantEditor;
import org.openscada.utils.filter.Filter;
import org.openscada.utils.filter.FilterAssertion;
import org.openscada.utils.filter.FilterExpression;
import org.openscada.utils.propertyeditors.DateEditor;
import org.openscada.utils.propertyeditors.PropertyEditorRegistry;
import org.openscada.utils.propertyeditors.UUIDEditor;

public class FilterUtils {

	private static PropertyEditorRegistry propertyEditorRegistry = new PropertyEditorRegistry();

	static {
		propertyEditorRegistry.registerCustomEditor(Date.class,
				new DateEditor());
		propertyEditorRegistry.registerCustomEditor(java.sql.Date.class,
				new DateEditor());
		propertyEditorRegistry.registerCustomEditor(Calendar.class,
				new DateEditor());
		propertyEditorRegistry.registerCustomEditor(UUID.class,
				new UUIDEditor());
        propertyEditorRegistry.registerCustomEditor(Variant.class,
                new VariantEditor());
	}
	/**
	 * converts string values in filter to actual Variant Values
	 * 
	 * @param filter
	 * @return
	 */
	public static void toVariant(Filter filter) {
		if (filter.isAssertion()) {
			FilterAssertion filterAssertion = (FilterAssertion) filter;
			if (filterAssertion.getValue() instanceof String) {
				if ("id".equals(filterAssertion.getAttribute())) {
					PropertyEditor pe = propertyEditorRegistry
							.findCustomEditor(UUID.class);
					pe.setAsText((String) filterAssertion.getValue());
					filterAssertion.setValue(pe.getValue());
				} else if ("sourceTimestamp".equals(filterAssertion
						.getAttribute())
						|| "entryTimestamp".equals(filterAssertion
								.getAttribute())) {
					PropertyEditor pe = propertyEditorRegistry
							.findCustomEditor(Date.class);
					pe.setAsText((String) filterAssertion.getValue());
					filterAssertion.setValue(pe.getValue());
				} else {
					VariantEditor ve = new VariantEditor();
					ve.setAsText((String) filterAssertion.getValue());
					filterAssertion.setValue(ve.getValue());
				}
			}
		} else {
			FilterExpression filterExpression = (FilterExpression) filter;
			for (Filter child : filterExpression.getFilterSet()) {
				toVariant(child);
			}
		}
	}
}
