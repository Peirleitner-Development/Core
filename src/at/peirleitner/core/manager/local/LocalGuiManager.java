package at.peirleitner.core.manager.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Material;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.local.GUI;
import at.peirleitner.core.util.local.ItemBuilder;
import at.peirleitner.core.util.user.LanguagePhrase;
import at.peirleitner.core.util.user.User;
import at.peirleitner.core.util.user.UserStatistic;
import net.md_5.bungee.api.ChatColor;

public class LocalGuiManager {

	public static final String GUI_STATISTICS = "My Statistiscs";

	public final GUI getStatisticMainGUI(@Nonnull User user) {

		GUI gui = new GUI();
		gui.setTitle(GUI_STATISTICS);

		for (SaveType saveType : Core.getInstance().getSaveTypes()) {

			gui.addItem(new ItemBuilder(Material.valueOf(saveType.getIconName()))
					.name(ChatColor.DARK_AQUA + saveType.getName()).addItemFlags().build());

		}

		return gui;
	}

	public final GUI getStatisticsGUI(@Nonnull User user, @Nonnull SaveType saveType) {

		GUI gui = new GUI();
		gui.setTitle(GUI_STATISTICS);

		gui.setItem(1,
				new ItemBuilder(Material.RED_BED).name(LanguagePhrase.BACK.getTranslatedText(user.getUUID())).build());

		Collection<UserStatistic> statisics = Core.getInstance().getStatSystem().getStatistics(user.getUUID(),
				saveType);
		List<String> desc = new ArrayList<>();

		if (statisics.isEmpty()) {

			gui.setItem(22, new ItemBuilder(Material.BARRIER)
					.name(ChatColor.GRAY + LanguagePhrase.NONE.getTranslatedText(user.getUUID())).build());

			return gui;
		}

		for (UserStatistic us : statisics) {

			desc.clear();

			String[] description = Core.getInstance().getLanguageManager()
					.getMessage(Core.getInstance().getPluginName(), user.getLanguage(),
							"gui.statistics.item.statistic.description",
							Arrays.asList(us.getStatistic().getDescription(),
									GlobalUtils.getFormatedDate(us.getStatistic().getCreated()),
									GlobalUtils.getFormatedDate(us.getFirstAdded()),
									GlobalUtils.getFormatedDate(us.getLastAdded()), "" + us.getAmount()))
					.split("\n");

			for (String s : description) {
				desc.add(s);
			}

			gui.addItem(new ItemBuilder(Material.valueOf(us.getStatistic().getIconName()))
					.name(ChatColor.DARK_AQUA + us.getStatistic().getDisplayName()).lore(desc).addItemFlags().build());
		}

		return gui;
	}

}
