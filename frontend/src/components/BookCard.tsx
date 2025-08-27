import React from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';

interface BookCardProps {
  book: {
    id: string;
    title: string;
    author: string;
    description: string;
    imageUrl?: string;
  };
}

const BookCard: React.FC<BookCardProps> = ({ book }) => {
  return (
    <div className="border rounded-lg p-4 shadow-sm flex flex-col">
      <h3 className="text-xl font-semibold mb-2">{book.title}</h3>
      <p className="text-gray-600 mb-2">{book.author}</p>
      {book.imageUrl && (
        <img src={book.imageUrl} alt={book.title} className="w-full h-48 object-cover rounded-md mb-4" />
      )}
      <p className="text-gray-700 flex-grow mb-4">{book.description}</p>
      <div className="flex justify-end mt-auto">
        <Link href={`/books/${book.id}/review`} passHref>
          <Button variant="outline">리뷰 작성</Button>
        </Link>
      </div>
    </div>
  );
};

export default BookCard;
