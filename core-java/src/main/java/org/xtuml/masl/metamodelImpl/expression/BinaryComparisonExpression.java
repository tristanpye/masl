//
// File: BinaryComparisonExpression.java
//
// UK Crown Copyright (c) 2008. All Rights Reserved.
//
package org.xtuml.masl.metamodelImpl.expression;

import org.xtuml.masl.metamodelImpl.common.Position;
import org.xtuml.masl.metamodelImpl.error.SemanticError;
import org.xtuml.masl.metamodelImpl.error.SemanticErrorCode;
import org.xtuml.masl.metamodelImpl.type.AnonymousStructure;
import org.xtuml.masl.metamodelImpl.type.AnyInstanceType;
import org.xtuml.masl.metamodelImpl.type.BasicType;
import org.xtuml.masl.metamodelImpl.type.BooleanType;
import org.xtuml.masl.metamodelImpl.type.DictionaryType;
import org.xtuml.masl.metamodelImpl.type.DurationType;
import org.xtuml.masl.metamodelImpl.type.EnumerateType;
import org.xtuml.masl.metamodelImpl.type.RealType;
import org.xtuml.masl.metamodelImpl.type.SequenceType;
import org.xtuml.masl.metamodelImpl.type.TimestampType;
import org.xtuml.masl.metamodelImpl.type.UserDefinedType;
import org.xtuml.masl.metamodelImpl.type.WCharacterType;
import org.xtuml.masl.metamodelImpl.type.WStringType;


public class BinaryComparisonExpression extends BinaryExpression
{

  public BinaryComparisonExpression ( Expression lhs, final OperatorRef operator, Expression rhs ) throws SemanticError
  {
    super(lhs.getPosition(), operator);

    rhs = rhs.resolve(lhs.getType());
    lhs = lhs.resolve(rhs.getType());

    setLhs(lhs);
    setRhs(rhs);

    checkOperand(getLhs().getType(), getLhs().getPosition());
    checkOperand(getRhs().getType(), getRhs().getPosition());

    if ( !(getLhs().getType().isAssignableFrom(getRhs()) || getRhs().getType().isAssignableFrom(getLhs())) )
    {
      throw new SemanticError(SemanticErrorCode.OperatorOperandsNotCompatible,
                              getOperatorRef().getPosition(),
                              getLhs().getType(),
                              getRhs()
                                      .getType(),
                              getOperatorRef());
    }

  }

  private void checkOperand ( final BasicType opType, final Position position ) throws SemanticError
  {

    if ( !(RealType.createAnonymous().isConvertibleFrom(opType)
           || DurationType.createAnonymous().isConvertibleFrom(opType)
           || TimestampType.createAnonymous().isConvertibleFrom(opType)
           || WCharacterType.createAnonymous().isConvertibleFrom(opType)
           || WStringType.createAnonymous().isConvertibleFrom(opType) || (opType.getPrimitiveType() instanceof UserDefinedType && ((UserDefinedType)opType.getPrimitiveType()).getDefinedType() instanceof EnumerateType)) )
    {
      if ( opType.getPrimitiveType() instanceof AnonymousStructure )
      {
        final AnonymousStructure struct = (AnonymousStructure)opType.getPrimitiveType();
        for ( final BasicType elt : struct.getElements() )
        {
          checkOperand(elt, position);
        }
      }
      else if ( opType.getPrimitiveType() instanceof SequenceType )
      {
        checkOperand(opType.getContainedType(), position);
      }
      else if ( opType.getPrimitiveType() instanceof DictionaryType )
      {
        checkOperand(((DictionaryType)opType.getPrimitiveType()).getKeyType(), position);
        checkOperand(((DictionaryType)opType.getPrimitiveType()).getValueType(), position);
      }
      else if ( !((getOperator() == Operator.EQUAL || getOperator() == Operator.NOT_EQUAL) &&
                  (AnyInstanceType.createAnonymous().isConvertibleFrom(opType)) || BooleanType.createAnonymous()
                                                                                              .isConvertibleFrom(opType)) )
      {
        throw new SemanticError(SemanticErrorCode.ComparisonNotValidForType, position, opType, getOperatorRef());
      }
    }

  }

  @Override
  public BooleanLiteral evaluate ()
  {
    final LiteralExpression lhsVal = getLhs().evaluate();
    final LiteralExpression rhsVal = getRhs().evaluate();

    if ( lhsVal instanceof NumericLiteral && rhsVal instanceof NumericLiteral )
    {
      if ( lhsVal instanceof RealLiteral || rhsVal instanceof RealLiteral )
      {
        final double lhsNum = ((NumericLiteral)lhsVal).getValue().doubleValue();
        final double rhsNum = ((NumericLiteral)rhsVal).getValue().doubleValue();

        switch ( getOperator() )
        {
          case LESS_THAN:
            return new BooleanLiteral(lhsNum < rhsNum);
          case LESS_THAN_OR_EQUAL:
            return new BooleanLiteral(lhsNum <= rhsNum);
          case GREATER_THAN:
            return new BooleanLiteral(lhsNum > rhsNum);
          case GREATER_THAN_OR_EQUAL:
            return new BooleanLiteral(lhsNum >= rhsNum);
          case EQUAL:
            return new BooleanLiteral(lhsNum == rhsNum);
          case NOT_EQUAL:
            return new BooleanLiteral(lhsNum != rhsNum);
          default:
            assert false : "Invalid comparison operator " + getOperator();
        }
      }
      else
      {
        final long lhsNum = ((NumericLiteral)lhsVal).getValue().longValue();
        final long rhsNum = ((NumericLiteral)rhsVal).getValue().longValue();

        switch ( getOperator() )
        {
          case LESS_THAN:
            return new BooleanLiteral(lhsNum < rhsNum);
          case LESS_THAN_OR_EQUAL:
            return new BooleanLiteral(lhsNum <= rhsNum);
          case GREATER_THAN:
            return new BooleanLiteral(lhsNum > rhsNum);
          case GREATER_THAN_OR_EQUAL:
            return new BooleanLiteral(lhsNum >= rhsNum);
          case EQUAL:
            return new BooleanLiteral(lhsNum == rhsNum);
          case NOT_EQUAL:
            return new BooleanLiteral(lhsNum != rhsNum);
          default:
            assert false : "Invalid comparison operator " + getOperator();
        }
      }
    }
    else if ( (lhsVal instanceof StringLiteral || lhsVal instanceof CharacterLiteral)
              && (rhsVal instanceof StringLiteral || rhsVal instanceof CharacterLiteral) )
    {
      final String lhsStr = (lhsVal instanceof StringLiteral) ?
                                                             ((StringLiteral)lhsVal).getValue() :
                                                             ("" + ((CharacterLiteral)lhsVal).getValue());
      final String rhsStr = (rhsVal instanceof StringLiteral) ?
                                                             ((StringLiteral)rhsVal).getValue()
                                                             : ("" + ((CharacterLiteral)rhsVal)
                                                                                               .getValue());

      switch ( getOperator() )
      {
        case LESS_THAN:
          return new BooleanLiteral(lhsStr.compareTo(rhsStr) < 0);
        case LESS_THAN_OR_EQUAL:
          return new BooleanLiteral(lhsStr.compareTo(rhsStr) <= 0);
        case GREATER_THAN:
          return new BooleanLiteral(lhsStr.compareTo(rhsStr) > 0);
        case GREATER_THAN_OR_EQUAL:
          return new BooleanLiteral(lhsStr.compareTo(rhsStr) >= 0);
        case EQUAL:
          return new BooleanLiteral(lhsStr.compareTo(rhsStr) == 0);
        case NOT_EQUAL:
          return new BooleanLiteral(lhsStr.compareTo(rhsStr) != 0);
        default:
          assert false : "Invalid comparison operator " + getOperator();
      }
    }
    return null;
  }

  @Override
  public BasicType getType ()
  {
    return BooleanType.createAnonymous();
  }

}
