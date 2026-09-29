package com.roboo.mineshafttycoonutils;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SearchArgumentType implements ArgumentType<String> {

    private static final Set<String> RESERVED = Set.of(
            "resetfishinghud",
            "resetprofittracker",
            "resetpve",
            "targetheat",
            "hudpositionsreset",
            "hudscalereset",
            "debug",
            "edithud"
    );

    private static final SimpleCommandExceptionType RESERVED_ERROR =
            new SimpleCommandExceptionType(new LiteralMessage("Reserved subcommand"));

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String text = reader.getRemaining();
        String first = text.split(" ", 2)[0];
        if (RESERVED.contains(first)) throw RESERVED_ERROR.createWithContext(reader);

        reader.setCursor(reader.getTotalLength());
        return text;
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("mining");
    }
}