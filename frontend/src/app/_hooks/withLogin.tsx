"use client"

import { Card, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "./auth-context"
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";

export default function withLogin<P extends object>(
    Component: React.ComponentType<P>,
){

    return function WithLoginComponent(props:P){
        const {isLoggedIn, isLoading} = useAuth();
        const router = useRouter();
        const onNavigate = ()=>{
            router.push("/login");
        }

        if (isLoading) {
            return <div>Loading...</div>;
        }

        if (!isLoggedIn){
            return <div className="flex w-full justify-center mt-10">
                <Card className="w-full max-w-md">
                    <CardHeader>
                        <CardTitle>로그인 후 이용해주세요.</CardTitle>
                    </CardHeader>
                    <CardFooter>
                        <Button onClick={onNavigate}>로그인 하러 가기</Button>
                    </CardFooter>
                </Card>
                
            </div>
        }
        return <Component {...props}/>
    }
}