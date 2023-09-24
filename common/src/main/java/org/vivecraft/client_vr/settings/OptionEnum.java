package org.vivecraft.client_vr.settings;

import org.vivecraft.client.utils.LangHelper;

import java.lang.reflect.Method;

public interface OptionEnum<T extends Enum<T>> {
	default String getLangKey() {
		return switch (name().toLowerCase()) {
			case "yes" -> LangHelper.YES_KEY;
			case "no" -> LangHelper.NO_KEY;
			case "on" -> LangHelper.ON_KEY;
			case "off" -> LangHelper.OFF_KEY;
			default -> {
				Class<?> cls = getClass();
				yield "vivecraft.options." + (cls.isAnonymousClass() ? cls.getSuperclass() : cls).getSimpleName().toLowerCase() + "." + name().toLowerCase().replace("_", "");
			}
		};
	}

	default T getNext() {
		if (ordinal() == getValues().length - 1)
			return getValues()[0];
		return getValues()[ordinal() + 1];
	}

	default T getPrevious() {
		if (ordinal() == 0)
			return getValues()[getValues().length - 1];
		return getValues()[ordinal() - 1];
	}

	// Actual Java life hacks
	String name();
	int ordinal();

	// The values method only exists at runtime, so we'll call it via reflection
	// This is absolute fuckery, but it works :)
	@SuppressWarnings("unchecked")
	default T[] getValues() {
		try {
			Method m = getClass().getMethod("values");
			return (T[])m.invoke(null);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		}
	}
}
