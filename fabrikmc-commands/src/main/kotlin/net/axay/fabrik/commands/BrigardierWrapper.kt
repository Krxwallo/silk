package net.axay.fabrik.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

private typealias SCS = ServerCommandSource

/**
 * Create a new command.
 *
 * @param name the name of the root command
 * @param register if true, the command will automatically be registered
 */
inline fun command(
    name: String,
    register: Boolean = false,
    builder: LiteralArgumentBuilder<SCS>.() -> Unit
): LiteralArgumentBuilder<SCS> =
    CommandManager.literal(name).apply(builder).apply {
        if (register)
            setupRegistrationCallback()
    }

/**
 * Add custom execution logic for this command.
 */
inline fun ArgumentBuilder<SCS, *>.simpleExecutes(
    crossinline executor: (CommandContext<SCS>) -> Unit
) {
    executes wrapped@{
        executor.invoke(it)
        return@wrapped 1
    }
}

/**
 * Add a new literal to this command.
 *
 * @param name the name of the literal
 */
inline fun ArgumentBuilder<SCS, *>.literal(
    name: String,
    builder: LiteralArgumentBuilder<SCS>.() -> Unit
) {
    then(command(name, false, builder))
}

/**
 * Add an argument.
 *
 * @param name the name of the argument
 * @param type the type of the argument - e.g. IntegerArgumentType.integer() or StringArgumentType.string()
 */
inline fun <T> ArgumentBuilder<SCS, *>.argument(
    name: String,
    type: ArgumentType<T>,
    builder: RequiredArgumentBuilder<SCS, T>.() -> Unit
) {
    then(CommandManager.argument(name, type).apply(builder))
}

/**
 * Add custom suggestion logic for an argument.
 */
inline fun RequiredArgumentBuilder<SCS, *>.simpleSuggests(
    crossinline suggestionBuilder: (CommandContext<SCS>) -> Iterable<Any?>
) {
    suggests { context, builder ->
        suggestionBuilder.invoke(context).forEach {
            if (it is Int)
                builder.suggest(it)
            else
                builder.suggest(it.toString())
        }
        builder.buildFuture()
    }
}

/**
 * Get the value of this argument.
 */
inline fun <reified T> CommandContext<SCS>.getArgument(name: String): T = getArgument(name, T::class.java)