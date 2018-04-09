package model.shared


fun String.isLong() = toLongOrNull() != null
fun String.isInt() = toIntOrNull() != null
fun String.isDouble() = toDoubleOrNull() != null
fun String.isFloat() = toFloatOrNull() != null

open class State private constructor(symbolSet: Set<String>, initialState: String) {

    var currentState = initialState
        set(value) {
            if (symbols.contains(value)) field = prefix + value
        }

    val symbols = symbolSet

    var prefix = ""

    private fun isLettersWithNumericalEndForm(str: String): Boolean = isThePattern(str,"[a-zA-Z]+\\d+")
    private fun isThePattern(str: String, pattern: String): Boolean = pattern.toRegex().matches(str)

    private fun parseIntVariableAtEnd(str: String): Int?{
        var res = ""
        if (isLettersWithNumericalEndForm(str)){
            for (i in 0..(str.length - 1)){
                if (str[i].isDigit()){
                    while (i <= str.length - 1){
                        res += str[i]
                    }
                    return res.toInt()
                }
            }
        }
        return null
    }
    private fun parseNonnumercialAtHead(str: String): String?{
        var res = ""
        if (isLettersWithNumericalEndForm(str)){
            for (i in 0..(str.length - 1)){
                if (!str[i].isDigit()){
                    res += str[i]
                }else{
                    return res
                }
            }
        }
        return null
    }

    operator fun plus(another: Int):State{
        if (isLettersWithNumericalEndForm(currentState)){
            return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another)))
        }
        if (currentState.isDouble()){
            return State(symbols,(currentState.toDouble() + another).toString())
        }
        if (currentState.isLong()){
            return State(symbols,(currentState.toLong() + another).toString())
        }
        if (currentState.isInt()){
            return State(symbols,(currentState.toInt() + another).toString())

        }
        if (currentState.isFloat()){
            return State(symbols,(currentState.toFloat() + another).toString())

        }

        throw IllegalArgumentException("Cannot add because the state is not numerical.")
    }
    operator fun minus(another: Int):State{
        if (isLettersWithNumericalEndForm(currentState)){
            return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! - another)))
        }
        if (currentState.isDouble()){
            return State(symbols,(currentState.toDouble() - another).toString())
        }
        if (currentState.isLong()){
            return State(symbols,(currentState.toLong() - another).toString())
        }
        if (currentState.isInt()){
            return State(symbols,(currentState.toInt() - another).toString())

        }
        if (currentState.isFloat()){
            return State(symbols,(currentState.toFloat() - another).toString())

        }

        throw IllegalArgumentException("Cannot add because the state is not numerical.")
    }

    operator fun minus(another: State): State{
        if (isLettersWithNumericalEndForm(currentState)){
            if (another.currentState.isInt()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! - another.currentState.toInt())))
            }

            if (another.currentState.isDouble()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! - another.currentState.toDouble())))
            }

            if (another.currentState.isLong()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! - another.currentState.toLong())))
            }
            if (another.currentState.isFloat()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! - another.currentState.toFloat())))
            }
        }
        if (isLettersWithNumericalEndForm(another.currentState)){
            if (currentState.isInt()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! - currentState.toInt())))
            }

            if (currentState.isDouble()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! - currentState.toDouble())))
            }

            if (currentState.isLong()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! - currentState.toLong())))
            }
            if (currentState.isFloat()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! - currentState.toFloat())))
            }
        }
        if (currentState.isDouble()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toDouble() - another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toDouble() - another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toDouble() - another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toDouble() - another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isLong()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toLong() - another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toLong() - another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toLong() - another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toLong() - another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isInt()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toInt() - another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toInt() - another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toInt() - another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toInt() - another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isFloat()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toFloat() - another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toFloat() - another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toFloat() - another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toFloat() - another.currentState.toFloat()).toString())
            }
        }
        if (isLettersWithNumericalEndForm(currentState) && isLettersWithNumericalEndForm(another.currentState)){
            return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! - parseIntVariableAtEnd(another.currentState)!!)))

        }

        throw IllegalArgumentException("Cannot add because the state is not numerical.")
    }
    operator fun plus(another: State):State{
        if (isLettersWithNumericalEndForm(currentState) && isLettersWithNumericalEndForm(another.currentState)){
            return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + parseIntVariableAtEnd(another.currentState)!!)))

        }
        if (isLettersWithNumericalEndForm(currentState)){
            if (another.currentState.isInt()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.currentState.toInt())))
            }

            if (another.currentState.isDouble()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.currentState.toDouble())))
            }

            if (another.currentState.isLong()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.currentState.toLong())))
            }
            if (another.currentState.isFloat()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.currentState.toFloat())))
            }
        }
        if (isLettersWithNumericalEndForm(another.currentState)){
            if (currentState.isInt()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! + currentState.toInt())))
            }

            if (currentState.isDouble()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! + currentState.toDouble())))
            }

            if (currentState.isLong()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! + currentState.toLong())))
            }
            if (currentState.isFloat()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! + currentState.toFloat())))
            }
        }
        if (currentState.isInt()){
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toInt() + another.currentState.toInt()).toString())
            }
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toInt() + another.currentState.toDouble()).toString())
            }

            if (another.currentState.isLong()){
                return State(symbols,(currentState.toInt() + another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toInt() + another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isDouble()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toDouble() + another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toDouble() + another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toDouble() + another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toDouble() + another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isLong()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toLong() + another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toLong() + another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toLong() + another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toLong() + another.currentState.toFloat()).toString())
            }
        }

        if (currentState.isFloat()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toFloat() + another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toFloat() + another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toFloat() + another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toFloat() + another.currentState.toFloat()).toString())
            }
        }

        throw IllegalArgumentException("Cannot add because the state is not numerical.")
    }
    operator fun times(another: State):State{
        if (currentState.isDouble()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toDouble() * another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toDouble() * another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toDouble() * another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toDouble() * another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isLong()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toLong() * another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toLong() * another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toLong() * another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toLong() * another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isInt()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toInt() * another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toInt() * another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toInt() * another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toInt() * another.currentState.toFloat()).toString())
            }
        }
        if (currentState.isFloat()){
            if (another.currentState.isDouble()){
                return State(symbols,(currentState.toFloat() * another.currentState.toDouble()).toString())
            }
            if (another.currentState.isInt()){
                return State(symbols,(currentState.toFloat() * another.currentState.toInt()).toString())
            }
            if (another.currentState.isLong()){
                return State(symbols,(currentState.toFloat() * another.currentState.toLong()).toString())
            }
            if (another.currentState.isFloat()){
                return State(symbols,(currentState.toFloat() * another.currentState.toFloat()).toString())
            }
        }
        if (isLettersWithNumericalEndForm(currentState) && isLettersWithNumericalEndForm(another.currentState)){
            return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! * parseIntVariableAtEnd(another.currentState)!!)))

        }
        if (isLettersWithNumericalEndForm(currentState)){
            if (another.currentState.isInt()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! * another.currentState.toInt())))
            }

            if (another.currentState.isDouble()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! * another.currentState.toDouble())))
            }

            if (another.currentState.isLong()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! * another.currentState.toLong())))
            }
            if (another.currentState.isFloat()){
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! * another.currentState.toFloat())))
            }
        }
        if (isLettersWithNumericalEndForm(another.currentState)){
            if (currentState.isInt()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! * currentState.toInt())))
            }

            if (currentState.isDouble()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! * currentState.toDouble())))
            }

            if (currentState.isLong()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! * currentState.toLong())))
            }
            if (currentState.isFloat()){
                return State(symbols,(parseNonnumercialAtHead(another.currentState)+ (parseIntVariableAtEnd(another.currentState)!! * currentState.toFloat())))
            }
        }
        throw IllegalArgumentException("Cannot add because the state is not numerical.")
    }

    operator fun rem(value: Int): State{
        if (isLettersWithNumericalEndForm(currentState))
            return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! % value)))
        when{
            this.currentState.isInt() -> return State(symbols, (currentState.toInt() % value).toString())
            this.currentState.isDouble() -> return State(symbols, (currentState.toDouble() % value).toString())
            this.currentState.isLong() -> return State(symbols, (currentState.toLong() % value).toString())
            this.currentState.isFloat() -> return State(symbols, (currentState.toFloat() % value).toString())
        }
        throw IllegalArgumentException("Cannot get reminder because the state is not numerical.")
    }


    operator fun div(another: State): State{
        if (isLettersWithNumericalEndForm(currentState) && isLettersWithNumericalEndForm(another.currentState)){
            val value =  parseIntVariableAtEnd(another.currentState)
            if (value!= 0)
                return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! / parseIntVariableAtEnd(another.currentState)!!)))
            else throw IllegalArgumentException("Cannot divided by 0")
        }
        if (isLettersWithNumericalEndForm(currentState)){
            if (another.currentState.isInt()){
                val value = another.currentState.toInt()
                if(value != 0)
                    return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! / another.currentState.toInt())))
                else throw IllegalArgumentException("Cannot divided by 0")
            }

            if (another.currentState.isDouble()){
                val value = another.currentState.toDouble()
                if(value != 0.0)
                    return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! / another.currentState.toDouble())))
                else throw IllegalArgumentException("Cannot divided by 0.0")
            }

            if (another.currentState.isLong()){
                val value = another.currentState.toLong()
                if(value != 0L)
                    return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! / another.currentState.toLong())))
                else throw IllegalArgumentException("Cannot divided by 0L")
            }
            if (another.currentState.isFloat()){
                val value = another.currentState.toFloat()
                if(value != 0F)
                    return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! / another.currentState.toFloat())))
                else throw IllegalArgumentException("Cannot divided by 0")
            }
        }
        if (currentState.isInt()){
            if (another.currentState.isDouble()){
                val value = another.currentState.toDouble()
                if(value != 0.0)
                    return State(symbols,(currentState.toInt() / another.currentState.toDouble()).toString())
                else throw IllegalArgumentException("Cannot divided by 0.0")
            }
            if (another.currentState.isInt()){
                val value = another.currentState.toInt()
                if(value != 0)
                    return State(symbols,(currentState.toInt() / another.currentState.toInt()).toString())
                else throw IllegalArgumentException("Cannot divided by 0")
            }
            if (another.currentState.isLong()){
                val value = another.currentState.toLong()
                if(value != 0L)
                    return State(symbols,(currentState.toInt() / another.currentState.toLong()).toString())
                else throw IllegalArgumentException("Cannot divided by 0L")
            }
            if (another.currentState.isFloat()){
                val value = another.currentState.toFloat()
                if(value != 0F)
                    return State(symbols,(currentState.toInt() / another.currentState.toFloat()).toString())
                else throw IllegalArgumentException("Cannot divided by 0F")
            }
        }
        if (currentState.isDouble()){
            if (another.currentState.isDouble()){
                val value = another.currentState.toDouble()
                if(value != 0.0)
                    return State(symbols,(currentState.toDouble() / another.currentState.toDouble()).toString())
                else throw IllegalArgumentException("Cannot divided by 0.0")
            }
            if (another.currentState.isInt()){
                val value = another.currentState.toInt()
                if(value != 0)
                    return State(symbols,(currentState.toDouble() / another.currentState.toInt()).toString())
                else throw IllegalArgumentException("Cannot divided by 0")
            }
            if (another.currentState.isLong()){
                val value = another.currentState.toLong()
                if(value != 0L)
                    return State(symbols,(currentState.toDouble() / another.currentState.toLong()).toString())
                else throw IllegalArgumentException("Cannot divided by 0L")
            }
            if (another.currentState.isFloat()){
                val value = another.currentState.toFloat()
                if(value != 0F)
                    return State(symbols,(currentState.toDouble() / another.currentState.toFloat()).toString())
                else throw IllegalArgumentException("Cannot divided by 0F")
            }
        }
        if (currentState.isLong()){
            if (another.currentState.isDouble()){
                val value = another.currentState.toDouble()
                if(value != 0.0)
                    return State(symbols,(currentState.toLong() / another.currentState.toDouble()).toString())
                else throw IllegalArgumentException("Cannot divided by 0.0")
            }
            if (another.currentState.isInt()){
                val value = another.currentState.toInt()
                if(value != 0)
                    return State(symbols,(currentState.toLong() / another.currentState.toInt()).toString())
                else throw IllegalArgumentException("Cannot divided by 0")
            }
            if (another.currentState.isLong()){
                val value = another.currentState.toLong()
                if(value != 0L)
                    return State(symbols,(currentState.toLong() / another.currentState.toLong()).toString())
                else throw IllegalArgumentException("Cannot divided by 0L")
            }
            if (another.currentState.isFloat()){
                val value = another.currentState.toFloat()
                if(value != 0F)
                    return State(symbols,(currentState.toLong() / another.currentState.toFloat()).toString())
                else throw IllegalArgumentException("Cannot divided by 0F")
            }
        }
        if (currentState.isFloat()){
            if (another.currentState.isDouble()){
                val value = another.currentState.toDouble()
                if(value != 0.0)
                    return State(symbols,(currentState.toFloat() / another.currentState.toDouble()).toString())
                else throw IllegalArgumentException("Cannot divided by 0.0")
            }
            if (another.currentState.isInt()){
                val value = another.currentState.toInt()
                if(value != 0)
                    return State(symbols,(currentState.toFloat() / another.currentState.toInt()).toString())
                else throw IllegalArgumentException("Cannot divided by 0")
            }
            if (another.currentState.isLong()){
                val value = another.currentState.toLong()
                if(value != 0L)
                    return State(symbols,(currentState.toFloat() / another.currentState.toLong()).toString())
                else throw IllegalArgumentException("Cannot divided by 0L")
            }
            if (another.currentState.isFloat()){
                val value = another.currentState.toFloat()
                if(value != 0F)
                    return State(symbols,(currentState.toFloat() / another.currentState.toFloat()).toString())
                else throw IllegalArgumentException("Cannot divided by 0F")
            }
        }

        throw IllegalArgumentException("Cannot add because the state is not numerical.")

    }

    /* operator fun plus(another: String): State{

         if (currentState.isDouble()){
             if (another.isDouble()){
                 return State(symbols,(currentState.toDouble() + another.toDouble()).toString())
             }
             if (another.isInt()){
                 return State(symbols,(currentState.toDouble() + another.toInt()).toString())
             }
             if (another.isLong()){
                 return State(symbols,(currentState.toDouble() + another.toLong()).toString())
             }
             if (another.isFloat()){
                 return State(symbols,(currentState.toDouble() + another.toFloat()).toString())
             }
         }
         if (currentState.isLong()){
             if (another.isDouble()){
                 return State(symbols,(currentState.toLong() + another.toDouble()).toString())
             }
             if (another.isInt()){
                 return State(symbols,(currentState.toLong() + another.toInt()).toString())
             }
             if (another.isLong()){
                 return State(symbols,(currentState.toLong() + another.toLong()).toString())
             }
             if (another.isFloat()){
                 return State(symbols,(currentState.toLong() + another.toFloat()).toString())
             }
         }
         if (currentState.isInt()){
             if (another.isDouble()){
                 return State(symbols,(currentState.toInt() + another.toDouble()).toString())
             }
             if (another.isInt()){
                 return State(symbols,(currentState.toInt() + another.toInt()).toString())
             }
             if (another.isLong()){
                 return State(symbols,(currentState.toInt() + another.toLong()).toString())
             }
             if (another.isFloat()){
                 return State(symbols,(currentState.toInt() + another.toFloat()).toString())
             }
         }
         if (currentState.isFloat()){
             if (another.isDouble()){
                 return State(symbols,(currentState.toFloat() + another.toDouble()).toString())
             }
             if (another.isInt()){
                 return State(symbols,(currentState.toFloat() + another.toInt()).toString())
             }
             if (another.isLong()){
                 return State(symbols,(currentState.toFloat() + another.toLong()).toString())
             }
             if (another.isFloat()){
                 return State(symbols,(currentState.toFloat() + another.toFloat()).toString())
             }
         }
         if (isLettersWithNumericalEndForm(currentState)){
             if (another.isInt()){
                 return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.toInt())))
             }

             if (another.isDouble()){
                 return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.toDouble())))
             }

             if (another.isLong()){
                 return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.toLong())))
             }
             if (another.isFloat()){
                 return State(symbols,(parseNonnumercialAtHead(currentState)+ (parseIntVariableAtEnd(currentState)!! + another.toFloat())))
             }
         }
         if (isLettersWithNumericalEndForm(another)){
             if (currentState.isInt()){
                 return State(symbols,(parseNonnumercialAtHead(another)+ (parseIntVariableAtEnd(another)!! + currentState.toInt())))
             }

             if (currentState.isDouble()){
                 return State(symbols,(parseNonnumercialAtHead(another)+ (parseIntVariableAtEnd(another)!! + currentState.toDouble())))
             }

             if (currentState.isLong()){
                 return State(symbols,(parseNonnumercialAtHead(another)+ (parseIntVariableAtEnd(another)!! + currentState.toLong())))
             }
             if (currentState.isFloat()){
                 return State(symbols,(parseNonnumercialAtHead(another)+ (parseIntVariableAtEnd(another)!! + currentState.toFloat())))
             }
         }
         throw IllegalArgumentException("Cannot add because the state is not numerical.")

     }*/

    //Factory Design Pattern
    companion object {
        fun createState(symbols: Set<String>, initialState: String): State {
            if (symbols.isEmpty() || !symbols.contains(initialState))
                throw IllegalArgumentException()
            return State(symbols, initialState)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is State) {
            return this.currentState == other.currentState && this.prefix == other.prefix
        }
        return false
    }

    override fun hashCode(): Int {
        return 31 * currentState.hashCode() + prefix.hashCode()
    }

    override fun toString(): String {
        return prefix + currentState
    }

}