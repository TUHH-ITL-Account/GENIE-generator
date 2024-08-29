package generator.types;

import fdl.fdl._symboltable.CharacteristicsSymbol;
import fdl.fdl._symboltable.ConstantNumberSymbol;
import fdl.fdl._symboltable.DefinitionSymbol;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.ExamplesSymbol;
import fdl.fdl._symboltable.FigureSymbol;
import fdl.fdl._symboltable.HierarchySymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl._symboltable.PatternSymbol;
import fdl.fdl._symboltable.ProgressionSymbol;
import fdl.fdl._symboltable.SequenceSymbol;
import fdl.fdl._symboltable.TradeOffSymbol;
import fdl.fdl._symboltable.VariableGroupsSymbol;

public class FdlDslSymbolHelper {

  /**
   * Checks the Fdl-type of a given symbol.
   *
   * @param symbol a symbol from the Fdl-Dsl
   * @return a Fdl-type as defined in the enum FdlTypes
   */
  public static FdlTypes getFdlType(ICommonFdlSymbol symbol) {
    if (symbol instanceof DefinitionSymbol) {
      return FdlTypes.DEFINITION;
    } else if (symbol instanceof CharacteristicsSymbol) {
      return FdlTypes.CHARACTERISTICS;
    } else if (symbol instanceof TradeOffSymbol) {
      return FdlTypes.TRADE_OFF;
    } else if (symbol instanceof SequenceSymbol) {
      return FdlTypes.SEQUENCE;
    } else if (symbol instanceof ExamplesSymbol) {
      return FdlTypes.EXAMPLES;
    } else if (symbol instanceof EquationSymbol) {
      return FdlTypes.EQUATION;
    } else if (symbol instanceof FigureSymbol) {
      return FdlTypes.FIGURE;
    } else if (symbol instanceof ConstantNumberSymbol) {
      return FdlTypes.CONSTANT;
    } else if (symbol instanceof PatternSymbol) {
      return FdlTypes.PATTERN;
    } else if (symbol instanceof HierarchySymbol) {
      return FdlTypes.HIERARCHY;
    } else if (symbol instanceof ProgressionSymbol) {
      return FdlTypes.PROGRESSION;
    } else if (symbol instanceof VariableGroupsSymbol) {
      return FdlTypes.VAR_GROUPS;
    } else {
      return FdlTypes.UNDEFINED;
    }
  }

  public enum FdlTypes {
    UNDEFINED, DEFINITION, CHARACTERISTICS, TRADE_OFF, SEQUENCE, EXAMPLES, EQUATION, FIGURE, CONSTANT,
    HIERARCHY, PROGRESSION, PATTERN, VAR_GROUPS
  }
}
