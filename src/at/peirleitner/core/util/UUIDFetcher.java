package at.peirleitner.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UUIDFetcher {

	private static HashMap<UUID, String> nameCache = new HashMap<>();
	private static HashMap<String, UUID> uuidCache = new HashMap<>();
	private static Gson gson = new GsonBuilder().create();

	public static String getName(UUID uuid) {
		if (nameCache.containsKey(uuid)) {
			return nameCache.get(uuid);
		}
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(
					"https://api.minetools.eu/uuid/" + uuid.toString().replaceAll("-", "")).openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			con.disconnect();
			reader.close();

			JsonObject main = gson.fromJson(builder.toString(), JsonElement.class).getAsJsonObject();
			String name = main.get("name").getAsString();
			String sId = main.get("id").getAsString();

			if (sId.equals("null") || sId == null || sId.toString().isEmpty() || sId.toString().equals("")) {
				return null;
			}

			UUID id = UUIDTypeAdapter.fromString(sId);

			if (name.equals("null") || name == null || name.isEmpty() || name.equals("")) {
				return null;
			}

			if (id.equals("null") || id == null || id.toString().isEmpty() || id.toString().equals("")) {
				return null;
			}
			nameCache.put(id, name);
			uuidCache.put(name.toLowerCase(), id);
			return name;
		} catch (Exception ex) {
		}
		return null;
	}

	public static void getName(UUID uuid, FetcherCallback callback) {
		new Thread(() -> {
			if (nameCache.containsKey(uuid)) {
				callback.onSuccess(nameCache.get(uuid), uuid);
				return;
			}
			try {
				HttpURLConnection con = (HttpURLConnection) new URL(
						"https://api.minetools.eu/uuid/" + uuid.toString().replaceAll("-", "")).openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line;
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				con.disconnect();
				reader.close();

				JsonObject main = gson.fromJson(builder.toString(), JsonElement.class).getAsJsonObject();
				String name = main.get("name").getAsString();
				String sId = main.get("id").getAsString();

				if (sId.equals("null") || sId == null || sId.toString().isEmpty() || sId.toString().equals("")) {
					return;
				}

				UUID id = UUIDTypeAdapter.fromString(sId);

				if (name.equals("null") || name == null || name.isEmpty() || name.equals("")) {
					callback.onFailure();
					return;
				}

				if (id.equals("null") || id == null || id.toString().isEmpty() || id.toString().equals("")) {
					callback.onFailure();
					return;
				}
				nameCache.put(id, name);
				uuidCache.put(name.toLowerCase(), id);
				callback.onSuccess(name, id);
				return;
			} catch (Exception ex) {
				callback.onFailure();
			}

		}).start();
	}

	public static void getUUID(String name, FetcherCallback callback) {
		new Thread(() -> {
			if (uuidCache.containsKey(name.toLowerCase())) {
				callback.onSuccess(nameCache.get(uuidCache.get(name.toLowerCase())), uuidCache.get(name.toLowerCase()));
				return;
			}
			try {
				HttpURLConnection con = (HttpURLConnection) new URL("https://api.minetools.eu/uuid/" + name)
						.openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line;
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				reader.close();
				con.disconnect();

				JsonObject main = gson.fromJson(builder.toString(), JsonElement.class).getAsJsonObject();
				String nme = main.get("name").getAsString();
				String sId = main.get("id").getAsString();

				if (sId.equals("null") || sId == null || sId.toString().isEmpty() || sId.toString().equals("")) {
					callback.onFailure();
					return;
				}

				UUID id = UUIDTypeAdapter.fromString(sId);

				if (name.equals("null") || name == null || name.isEmpty() || name.equals("")) {
					callback.onFailure();
					return;
				}

				if (id.equals("null") || id == null || id.toString().isEmpty() || id.toString().equals("")) {
					callback.onFailure();
					return;
				}
				nameCache.put(id, nme);
				uuidCache.put(nme.toLowerCase(), id);
				callback.onSuccess(nme, id);
			} catch (Exception ex) {
				callback.onFailure();
			}
		}).start();
	}

	public static UUID getUUID(String name) {
		if (uuidCache.containsKey(name.toLowerCase())) {
			return uuidCache.get(name.toLowerCase());
		}
		try {
			HttpURLConnection con = (HttpURLConnection) new URL("https://api.minetools.eu/uuid/" + name)
					.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
			con.disconnect();

			JsonObject main = gson.fromJson(builder.toString(), JsonElement.class).getAsJsonObject();
			String nme = main.get("name").getAsString();
			String sId = main.get("id").getAsString();

			if (sId.equals("null") || sId == null || sId.toString().isEmpty() || sId.toString().equals("")) {
				return null;
			}

			UUID id = UUIDTypeAdapter.fromString(sId);

			if (name.equals("null") || name == null || name.isEmpty() || name.equals("")) {
				return null;
			}

			if (id.equals("null") || id == null || id.toString().isEmpty() || id.toString().equals("")) {
				return null;
			}
			nameCache.put(id, nme);
			uuidCache.put(nme.toLowerCase(), id);
			return id;
		} catch (Exception ex) {
		}
		return null;
	}

	public static interface FetcherCallback {

		public void onSuccess(String name, UUID uuid);

		public void onFailure();

	}
}