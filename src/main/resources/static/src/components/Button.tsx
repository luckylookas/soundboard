import React, {ReactElement} from "react";

interface ButtonProps {
    children: ReactElement | string
    onClick: () => void
    color: string
    disabled?: boolean
}

export const Button = ({children, onClick, color, disabled}: ButtonProps) => {
    return <div aria-disabled={!!disabled} onClick={disabled ? () => {} : onClick} className={`
     cursor-${disabled ? 'not-allowed' : 'pointer'}
    select-none
    justify-center
    items-center
    flex p-2 m-2 
    rounded 
    drop-shadow-lg
    bg-${color}-${disabled ? '950' : '300'} 
    hover:bg-${color}-${disabled ? '950' : '200'} 
    active:bg-${disabled ? '950' : '400'} 
    text-lg 
    text-neutral-${disabled ? '500' : '800'}`}>
        {children}
    </div>
}

interface ToggleProps {
    children: ReactElement | string
    onClick: () => void
    color: string
    checked: boolean
}

export const Toggle = ({children, onClick, color, checked}: ToggleProps) => {
    return <div onClick={onClick} className={`
    ${checked ? 'shadow-inner' : 'drop-shadow-lg'}
    cursor-pointer
    select-none
    justify-center
    items-center
    flex p-2 m-2 
    rounded 
    bg-${color}-${checked ? '600' : '300'} 
    hover:bg-${color}-200
    text-lg 
    text-neutral-800`}>
        {children}
    </div>
}


interface SliderProps {
    children: ReactElement | string
    onChange: (next: number) => void
    value: number,
    color: string
}

export const Slider = ({children, onChange, color, value}: SliderProps) => {
    return <div className={`
    cursor-pointer
    select-none
    drop-shadow-lg
    justify-center
    items-center
    flex p-2 m-2`}>
        <label htmlFor={"volume"} className={`flex`}>{children}</label>
        <input className={`bg-neutral-200 w-full accent-${color}-600`} id={"volume"} name='volume' type={"range"} max={100} min={0} value={value}
               onChange={e => onChange(Number.parseInt(e.target.value))}/>
    </div>
}
