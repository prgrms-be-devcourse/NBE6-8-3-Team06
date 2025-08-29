"use client"

import { useState } from "react"

export default function Spoiler({children, enabled=true}:{children:React.ReactNode, enabled:boolean}){
    const [revealed, setRevealed] = useState(!enabled);

    return (
        <div
      onClick={() => setRevealed(true)}
      className={`relative cursor-pointer rounded-md p-2 select-none overflow-hidden`}
    >
      {!revealed && (
        <div className="absolute inset-0 z-10 flex items-center justify-center">
          <span className="text-black font-bold">클릭해서 보기</span>
        </div>
      )}
      <div className={`${revealed ? 'blur-none' : 'blur-sm'} transition duration-300`}>
        {children}
      </div>
    </div>

    )
}