package com.vsu.amm.data.storage;

import com.vsu.amm.Constants;
import com.vsu.amm.stat.SimpleCounterSet;
import org.apache.log4j.Logger;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vsu.amm.Utils.isNullOrEmpty;

/**
 * Created by Kzarw on 25.04.2015.
 */
public class StorageGenerator {

	private static final Logger log = Logger.getLogger(StorageGenerator.class);

	public static List<IDataStorage> generateStorages(Element element,
			Map<String, Integer> params) {
		if (element == null)
			return null;

		if (!Constants.STORAGES_TAG_NAME.equals(element.getName()))
			return null;

		List<Element> children = element.getChildren();
		if (children == null) {
			return null;
		}

		List<IDataStorage> storages = new ArrayList<>(children.size());

		for (Element child : children) {
			if (!"storage".equals(child.getName())) {
				log.warn("Unknown element " + child.getName() + ". Skipped.");
				continue;
			}
			String c = child.getAttributeValue("class");
			if (isNullOrEmpty(c)) {
				log.warn("Class name not set. Skipped.");
				continue;
			}
			Map<String, Integer> storageParams;

			String storageParamName;
			String storageParamValue;
			storageParams = new HashMap<>();

			for (Element storageParam : child.getChildren()) {
				if ("param".equals(storageParam.getName())) {
					storageParamName = storageParam.getAttributeValue("name");
					storageParamValue = storageParam.getAttributeValue("value");
					storageParams.put(storageParamName,
							Integer.parseInt(storageParamValue));
				}
			}

			IDataStorage storage = getDataStorage(c, storageParams);
			if (storage != null) {
				storage.setCounterSet(new SimpleCounterSet());
				storages.add(storage);
			}
		}

		return storages;
	}

	/**
	 * Метод получения хранилища по названию класса и параметрам Для стандартных
	 * хранилищ предусмотрено подставление суффикса
	 * Constants.DEFAULT_PACKAGE_NAME
	 * 
	 * @param storageName
	 * @param storageParams
	 * @return
	 */
	public static IDataStorage getDataStorage(String storageName,  Map<String, Integer> storageParams) {
		IDataStorage result;
		if (storageName == null)
			return null;
		try {
			Object o = Class.forName(storageName).newInstance();
			result = (IDataStorage) o;
			result.setStorageParams(storageParams);
			return result;
		} catch (Exception e) {
			try {
				Object o = Class.forName(
						Constants.DEFAULT_PACKAGE_NAME + storageName)
						.newInstance();
				result = (IDataStorage) o;
				result.setStorageParams(storageParams);
				return result;
			} catch (Exception ex) {

				log.error("Cannot find or create class");
			}
		}
		return null;
	}
}
