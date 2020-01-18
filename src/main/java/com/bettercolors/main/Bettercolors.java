package com.bettercolors.main;

import com.bettercolors.io.SettingsUtils;
import com.bettercolors.modules.*;
import com.bettercolors.modules.options.Option;
import com.bettercolors.modules.options.ToggleOption;
import com.bettercolors.view.Window;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Keyboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Mod(modid = Reference.MOD_ID,
	 name = Reference.NAME,
	 version = Reference.VERSION,
	 acceptedMinecraftVersions = Reference.ACCEPTED_VERSIONS)

public class Bettercolors {

    public final static String URL_PROBLEM = "Url problem (please contact developer).";
    public final static String INTERNET_PROBLEM = "No internet connection. :(";
    public final static String UNRELEASED = "Preview version (not released yet).";
    public final static String EMPTY_PAGE = "Github release page has an issue.";
    public final static String DOWNLOAD_URL = "https://github.com/N3ROO/Bettercolors/releases/latest";


    private final static ArrayList<Option> DEFAULT_ACTIVATION_STATUS;
    static{
        DEFAULT_ACTIVATION_STATUS = new ArrayList<>();
        DEFAULT_ACTIVATION_STATUS.add(new ToggleOption("", AimAssistance.class.getSimpleName(), true));
        DEFAULT_ACTIVATION_STATUS.add(new ToggleOption("", ClickAssistance.class.getSimpleName(), true));
        DEFAULT_ACTIVATION_STATUS.add(new ToggleOption("", AutoSprint.class.getSimpleName(), true));
        DEFAULT_ACTIVATION_STATUS.add(new ToggleOption("", AutoSword.class.getSimpleName(), true));
    }

    private ArrayList<Module> _modules;
    private Map<String, Boolean> _key_down;
    private final String WINDOW = "windowGUI";
    private Window _window;

    @EventHandler
	public void Init(FMLInitializationEvent event)
	{
	    // Registers the rest
		MinecraftForge.EVENT_BUS.register(this);
		// Registers tick event
        FMLCommonHandler.instance().bus().register(this);

        // Antialiasing font
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        // Settings management
        Map<String, String> options = SettingsUtils.getOptions();
        ArrayList<ArrayList<Option>> modules_options = new ArrayList<>();
        modules_options.add(AimAssistance.getDefaultOptions());
        modules_options.add(ClickAssistance.getDefaultOptions());
        modules_options.add(DEFAULT_ACTIVATION_STATUS);
        // There is no settings file, we need to create it, otherwise we only add the settings that are not already
        // in the settings file (can happen after updating the mod to a newer version)
        SettingsUtils.setOptions(modules_options, options != null);
        options = SettingsUtils.getOptions();

		// Mods initialisation
        int KEY_HOME = Keyboard.KEY_HOME;
        int KEY_PAGE_UP = 201;
		_modules = new ArrayList<>();
		_modules.add(new AimAssistance("Aim assistance", KEY_HOME, Boolean.parseBoolean(options.get(AimAssistance.class.getSimpleName())), options, "aim_symbol.png"));
        _modules.add(new ClickAssistance("Click assistance", KEY_PAGE_UP, Boolean.parseBoolean(options.get(ClickAssistance.class.getSimpleName())), options, "click_symbol.png"));
		_modules.add(new AutoSprint("Auto sprint", -1, Boolean.parseBoolean(options.get(AutoSprint.class.getSimpleName())), "sprint_symbol.png"));
		_modules.add(new AutoSword("Auto sword", -1, Boolean.parseBoolean(options.get(AutoSword.class.getSimpleName())), "sword_symbol.png"));

		// KeyEvent
        _key_down = new HashMap<>();
        for(Module module : _modules){
            _key_down.put(module.getClass().getSimpleName(), false);
        }
        _key_down.put(WINDOW, false);

		// AbstractWindow initialisation
        _window = new Window("Bettercolors " + Reference.VERSION, _modules, getVersionInformation());
	}

	@SubscribeEvent
	public void onClientTickEvent(ClientTickEvent event){
        for(Module mod : _modules){
            mod.updateKeyHandler();
            if(mod.getToggleKey() != -1) {
                if (Keyboard.isKeyDown(mod.getToggleKey())) {
                    _key_down.replace(mod.getClass().getSimpleName(), true);
                } else if (_key_down.get(mod.getClass().getSimpleName())) {
                    // KEY RELEASED !
                    mod.toggle();
                    _window.synchronizeComponents();
                    SettingsUtils.setOption(mod.getClass().getSimpleName(), Boolean.toString(mod.isActivated()));
                    _key_down.replace(mod.getClass().getSimpleName(), false);
                }
            }
        }

        if(Keyboard.isKeyDown(Keyboard.KEY_INSERT)){
            _key_down.replace(WINDOW, true);
        }else if(_key_down.get(WINDOW)){
            _key_down.replace(WINDOW, false);
            _window.toggle();
        }
	}

	@SubscribeEvent
	public void clientTick(final TickEvent event){
        for(Module mod : _modules){
            if(mod.isActivated()){
                mod.update();
            }
        }
	}

    /**
     * @return the last version tag from the github release page and the changelog associated.
     */
	private String[] getVersionInformation(){
        final String MC_PREFIX = "-MC";
        String last_version = "";
        String changelog = "";

        try{
            // Retrieve JSON
            URL url = new URL("https://api.github.com/repos/n3roo/bettercolors/releases");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String json = in.lines().collect(Collectors.joining());
            in.close();

            // Get last version from JSON
            String[] tags = json.split("\"tag_name\"");
            String[] bodies = json.split("\"body\"");

            int i = 0;
            boolean found = false;
            while(i < tags.length && !found){
                last_version = tags[i].split("\"")[1];
                if(last_version.endsWith(MC_PREFIX + Reference.MAIN_MC_VERSION)){
                    found = true;
                    changelog = bodies[i].split("\"")[1];
                }else{
                    i ++;
                }
            }

            if(!found){
                return new String[]{UNRELEASED, ""};
            }else{
                last_version = last_version.replace(MC_PREFIX + Reference.MAIN_MC_VERSION, "");
                changelog = changelog.replace("\\r", "");
            }
        } catch (MalformedURLException e) {
            return new String[]{URL_PROBLEM, ""};
        } catch (IOException e) {
            return new String[]{INTERNET_PROBLEM, ""};
        } catch (Exception e) {
            return new String[]{EMPTY_PAGE, ""};
        }

        return new String[]{last_version, changelog};
    }

    /**
     * It compares the two given versions, and return the difference :
     * int[a, b, c, d]
     * - a is the major version dif,
     * - b is the minor version dif,
     * - c is the patch version dif,
     * - d is the beta version dif.
     * If error, returns null.
     * @param current_version current version
     * @param last_version last version
     * @return the version difference
     */
    public static int[] compareVersions(String current_version, String last_version){
        int[] diff = {0, 0, 0, 0};

        String[] current_version_split = current_version.split("\\.");
        String[] last_version_split = last_version.split("\\.");

        if(current_version_split.length == 3 && last_version_split.length == 3) {
            diff[0] = Integer.parseInt(last_version_split[0]) -  Integer.parseInt(current_version_split[0]);
            diff[1] = Integer.parseInt(last_version_split[1]) -  Integer.parseInt(current_version_split[1]);
            diff[2] = Integer.parseInt(last_version_split[2].split("-")[0]) -  Integer.parseInt(current_version_split[2].split("-")[0]);

            int current_beta_number = current_version_split[2].split("-").length == 2 ? Integer.parseInt(current_version_split[2].split("-")[1].replace("b", "")) : 0;
            int last_beta_number = last_version_split[2].split("-").length == 2 ? Integer.parseInt(last_version_split[2].split("-")[1].replace("b", "")) : 0;
            diff[3] = last_beta_number - current_beta_number;
        } else {
            System.out.println("Error when comparing versions : expected a version format maj.min.patch(-bnumber), but received :");
            System.out.println("[" + current_version + "] and [" + last_version + "]");
            diff = null;
        }

        return diff;
    }
}
