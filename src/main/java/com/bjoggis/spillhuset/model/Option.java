package com.bjoggis.spillhuset.model;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public record Option(String name, String description, OptionType type, boolean required,
                     boolean autocomplete) {

}
