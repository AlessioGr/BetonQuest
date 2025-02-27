package org.betonquest.betonquest.variables;

import lombok.CustomLog;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.Point;
import org.betonquest.betonquest.api.Variable;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;
import org.betonquest.betonquest.id.ID;

import java.util.List;
import java.util.Locale;

/**
 * Allows you to display total amount of points or amount of points remaining to
 * some other amount.
 */
@SuppressWarnings("PMD.CommentRequired")
@CustomLog
public class PointVariable extends Variable {

    protected String category;
    protected Type type;
    protected int amount;

    @SuppressWarnings("PMD")
    public PointVariable(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        category = instruction.next();

        if (instruction.size() == 4) {
            final String packagePath = instruction.current();
            final String pointCategory = instruction.next();
            try {
                final ID id = new ID(instruction.getPackage(), packagePath + "." + pointCategory) {
                };
                category = id.getPackage().getName() + "." + pointCategory;
            } catch (final ObjectNotFoundException e) {
                LOG.warn(instruction.getPackage(), e.getMessage());
            }

        } else if (instruction.size() == 3) {
            if (category.contains("*")) {
                category = category.replace('*', '.');
            } else {
                category = instruction.getPackage().getName() + "." + category;
            }
        }


        if ("amount".equalsIgnoreCase(instruction.next())) {
            type = Type.AMOUNT;
        } else if (instruction.current().toLowerCase(Locale.ROOT).startsWith("left:")) {
            type = Type.LEFT;
            try {
                amount = Integer.parseInt(instruction.current().substring(5));
            } catch (final NumberFormatException e) {
                throw new InstructionParseException("Could not parse point amount", e);
            }
        } else {
            throw new InstructionParseException(String.format("Unknown variable type: '%s'",
                    instruction.current()));
        }
    }

    @Override
    public String getValue(final String playerID) {
        return getValue(BetonQuest.getInstance().getPlayerData(playerID).getPoints());
    }

    protected String getValue(final List<Point> points) {
        Point point = null;
        for (final Point p : points) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                point = p;
                break;
            }
        }
        int count = 0;
        if (point != null) {
            count = point.getCount();
        }
        switch (type) {
            case AMOUNT:
                return Integer.toString(count);
            case LEFT:
                return Integer.toString(amount - count);
            default:
                return "";
        }
    }

    protected enum Type {
        AMOUNT, LEFT
    }

}
